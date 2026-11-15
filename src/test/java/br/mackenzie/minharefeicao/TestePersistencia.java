package br.mackenzie.minharefeicao;

import br.mackenzie.minharefeicao.controle.AssinarPlanoController;
import br.mackenzie.minharefeicao.controle.PagamentoNaoAutorizadoException;
import br.mackenzie.minharefeicao.dominio.Assinante;
import br.mackenzie.minharefeicao.dominio.Assinatura;
import br.mackenzie.minharefeicao.dominio.Cardapio;
import br.mackenzie.minharefeicao.dominio.Endereco;
import br.mackenzie.minharefeicao.dominio.PreferenciaAlimentar;
import br.mackenzie.minharefeicao.dominio.StatusAssinatura;
import br.mackenzie.minharefeicao.dominio.StatusPedido;
import br.mackenzie.minharefeicao.persistencia.DadosIniciais;
import br.mackenzie.minharefeicao.persistencia.RepositorioAssinantes;
import br.mackenzie.minharefeicao.persistencia.RepositorioAssinaturas;
import br.mackenzie.minharefeicao.persistencia.RepositorioPlanos;
import br.mackenzie.minharefeicao.servico.GeradorProtocolo;
import br.mackenzie.minharefeicao.servico.OperadoraCartaoSimulada;
import br.mackenzie.minharefeicao.servico.ServicoSMSSimulado;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/**
 * Testes da camada de persistência em arquivo: grava uma assinatura em uma
 * "execução" e verifica se ela volta como objeto de domínio em outra execução,
 * com repositórios recriados a partir dos arquivos de texto.
 *
 * <p>Executa com: {@code java -cp build/classes br.mackenzie.minharefeicao.TestePersistencia}</p>
 */
public class TestePersistencia {

    private static int ok = 0, falhas = 0;

    public static void main(String[] args) throws IOException {
        Path pasta = Files.createTempDirectory("minha-refeicao-teste");
        try {
            executar("Arquivos de dados são criados na pasta configurada", () -> arquivosCriados(pasta));
            executar("Assinatura aprovada é recarregada do arquivo", () -> assinaturaAprovadaVolta(pasta));
            executar("Assinante volta com preferências e endereço", () -> assinanteVolta(pasta));
            executar("FA3: assinatura recusada permanece Aguardando Pagamento após recarga",
                    () -> recusaPersistida(pasta));
            executar("Registros preservam texto com separador e acentuação", () -> textoComSeparador(pasta));
        } finally {
            apagar(pasta);
        }
        System.out.printf("%nResultado: %d passaram, %d falharam%n", ok, falhas);
        if (falhas > 0) {
            System.exit(1);
        }
    }

    // ---------- cenários ----------

    private static void arquivosCriados(Path pasta) {
        contratar(pasta, "11987654321", "4111111111111111");
        verificar(Files.exists(pasta.resolve("assinantes.txt")), "assinantes.txt deveria existir");
        verificar(Files.exists(pasta.resolve("assinaturas.txt")), "assinaturas.txt deveria existir");
        verificar(Files.exists(pasta.resolve("itens_pedido.txt")), "itens_pedido.txt deveria existir");
        verificar(Files.exists(pasta.resolve("pagamentos.txt")), "pagamentos.txt deveria existir");
    }

    private static void assinaturaAprovadaVolta(Path pasta) {
        contratar(pasta, "11988887777", "4111111111111111");

        // Nova "execução": repositórios recriados apenas a partir dos arquivos
        RepositorioAssinaturas recarregado = abrirAssinaturas(pasta);
        List<Assinatura> assinaturas = recarregado.listarPorCelular("11988887777");
        verificar(!assinaturas.isEmpty(), "a assinatura deveria ter sido recarregada");

        Assinatura a = assinaturas.get(assinaturas.size() - 1);
        verificar(a.getStatus() == StatusAssinatura.ATIVA, "status deveria ser ATIVA, veio " + a.getStatus());
        verificar(a.getPedido().getStatus() == StatusPedido.APROVADO, "pedido deveria estar APROVADO");
        verificar(a.getProtocolo() != null && a.getProtocolo().startsWith("MR-"), "protocolo deveria ter sido gravado");
        verificar(a.getPrevisaoPrimeiraEntrega() != null, "previsão da primeira entrega deveria ter sido gravada");
        verificar(a.getPedido().getItens().size() == 3, "o pedido deveria voltar com 3 itens");
        verificar(a.getPagamentos().size() == 1 && a.getPagamentos().get(0).isAprovado(),
                "deveria haver um pagamento aprovado");
        verificar(a.getPagamentos().get(0).getCartaoMascarado().startsWith("****"),
                "o arquivo deve guardar apenas o cartão mascarado");
    }

    private static void assinanteVolta(Path pasta) {
        contratar(pasta, "11966665555", "4111111111111111");

        RepositorioAssinantes recarregado = new RepositorioAssinantes(pasta);
        Assinante assinante = recarregado.buscarPorCelular("11966665555")
                .orElseThrow(() -> new AssertionError("assinante não foi recarregado"));
        verificar(assinante.isCelularVerificado(), "celular deveria voltar como verificado");
        verificar(assinante.getPreferencias().contains(PreferenciaAlimentar.VEGETARIANA),
                "preferência vegetariana deveria ter sido gravada");
        verificar(assinante.getEnderecoEntrega() != null, "endereço deveria ter sido gravado");
        verificar(assinante.getEnderecoEntrega().getCidade().equals("São Paulo"),
                "a acentuação do endereço deveria ser preservada (UTF-8)");
    }

    private static void recusaPersistida(Path pasta) {
        AssinarPlanoController controller = montarSessao(pasta, "11955554444");
        montarPedido(controller);
        controller.informarEndereco(enderecoPadrao());
        try {
            controller.pagar("5555444433330000", "GABRIEL V SANTOS", "12/30", "123");
            throw new AssertionError("a operadora deveria ter recusado o cartão terminado em 0000");
        } catch (PagamentoNaoAutorizadoException esperado) {
            // FA3.3: a assinatura continua aguardando pagamento
        }

        RepositorioAssinaturas recarregado = abrirAssinaturas(pasta);
        Assinatura a = recarregado.listarPorCelular("11955554444").get(0);
        verificar(a.getStatus() == StatusAssinatura.AGUARDANDO_PAGAMENTO,
                "status deveria permanecer AGUARDANDO_PAGAMENTO, veio " + a.getStatus());
        verificar(a.getProtocolo() == null, "não deveria haver protocolo sem pagamento aprovado");
        verificar(a.getPagamentos().size() == 1 && !a.getPagamentos().get(0).isAprovado(),
                "a tentativa recusada deveria ter sido gravada");
    }

    private static void textoComSeparador(Path pasta) {
        AssinarPlanoController controller = montarSessao(pasta, "11944443333");
        montarPedido(controller);
        controller.informarEndereco(new Endereco("Rua São João | Fundos", "1000", "Bloco A | ap. 12",
                "Centro", "São Paulo", "SP", "01035-000"));

        RepositorioAssinantes recarregado = new RepositorioAssinantes(pasta);
        Assinante assinante = recarregado.buscarPorCelular("11944443333").orElseThrow();
        verificar(assinante.getEnderecoEntrega().getLogradouro().equals("Rua São João | Fundos"),
                "o separador '|' dentro do campo deveria ser preservado");
        verificar(assinante.getEnderecoEntrega().getComplemento().equals("Bloco A | ap. 12"),
                "o complemento com '|' deveria ser preservado");
    }

    // ---------- apoio ----------

    private static void contratar(Path pasta, String celular, String cartao) {
        AssinarPlanoController controller = montarSessao(pasta, celular);
        montarPedido(controller);
        controller.informarEndereco(enderecoPadrao());
        controller.pagar(cartao, "GABRIEL V SANTOS", "12/30", "123");
    }

    private static AssinarPlanoController montarSessao(Path pasta, String celular) {
        RepositorioPlanos planos = DadosIniciais.criarPlanos();
        Cardapio cardapio = DadosIniciais.criarCardapio();
        RepositorioAssinantes assinantes = new RepositorioAssinantes(pasta);
        RepositorioAssinaturas assinaturas = new RepositorioAssinaturas(pasta, assinantes, planos, cardapio);
        ServicoSMSSimulado sms = new ServicoSMSSimulado(false);

        AssinarPlanoController controller = new AssinarPlanoController(sms, new OperadoraCartaoSimulada(),
                planos, assinantes, assinaturas, cardapio, new GeradorProtocolo(), new Random());
        controller.informarCelular(celular);
        controller.validarCodigo(sms.getUltimoCodigoEnviado());
        return controller;
    }

    private static void montarPedido(AssinarPlanoController controller) {
        controller.selecionarPlano(1);
        controller.registrarPreferencias(EnumSet.of(PreferenciaAlimentar.VEGETARIANA));
        controller.adicionarAoPedido("P3", 5);
        controller.adicionarAoPedido("A2", 5);
        controller.adicionarAoPedido("S1", 5);
    }

    private static Endereco enderecoPadrao() {
        return new Endereco("Rua da Consolação", "930", "Prédio 31", "Consolação", "São Paulo", "SP", "01302-907");
    }

    private static RepositorioAssinaturas abrirAssinaturas(Path pasta) {
        RepositorioPlanos planos = DadosIniciais.criarPlanos();
        Cardapio cardapio = DadosIniciais.criarCardapio();
        return new RepositorioAssinaturas(pasta, new RepositorioAssinantes(pasta), planos, cardapio);
    }

    private static void apagar(Path pasta) throws IOException {
        if (Files.exists(pasta)) {
            try (var caminhos = Files.walk(pasta)) {
                caminhos.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignorado) {
                        // pasta temporária: falha na limpeza não invalida o teste
                    }
                });
            }
        }
    }

    // ---------- mini-harness ----------

    private static void executar(String nome, Runnable teste) {
        try {
            teste.run();
            ok++;
            System.out.println("[OK]    " + nome);
        } catch (Throwable e) {
            falhas++;
            System.out.println("[FALHA] " + nome + " -> " + e);
        }
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
