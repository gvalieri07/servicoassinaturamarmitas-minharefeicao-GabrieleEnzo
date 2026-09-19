package br.mackenzie.minharefeicao;

import br.mackenzie.minharefeicao.controle.AssinarPlanoController;
import br.mackenzie.minharefeicao.controle.ConfirmacaoAssinatura;
import br.mackenzie.minharefeicao.controle.PagamentoNaoAutorizadoException;
import br.mackenzie.minharefeicao.dominio.*;
import br.mackenzie.minharefeicao.persistencia.DadosIniciais;
import br.mackenzie.minharefeicao.persistencia.RepositorioAssinantes;
import br.mackenzie.minharefeicao.persistencia.RepositorioAssinaturas;
import br.mackenzie.minharefeicao.servico.GeradorProtocolo;
import br.mackenzie.minharefeicao.servico.OperadoraCartaoSimulada;
import br.mackenzie.minharefeicao.servico.ServicoSMSSimulado;

import java.util.EnumSet;
import java.util.Random;

/**
 * Testes automatizados das regras do caso de uso, sem bibliotecas externas
 * (executa com: java -cp out br.mackenzie.minharefeicao.TesteRegrasNegocio).
 */
public class TesteRegrasNegocio {
    private static int ok = 0, falhas = 0;

    public static void main(String[] args) {
        executar("Fluxo principal ativa assinatura e aprova pedido", TesteRegrasNegocio::fluxoPrincipal);
        executar("FA1: código inválido reduz tentativas", TesteRegrasNegocio::fa1CodigoInvalido);
        executar("FA1.5: 3ª tentativa inválida exige novo código", TesteRegrasNegocio::fa1TresTentativas);
        executar("FA2: limite de refeições do plano", TesteRegrasNegocio::fa2Limite);
        executar("Filtro por preferências (vegetariana + sem lactose)", TesteRegrasNegocio::filtroPreferencias);
        executar("FA3: recusa mantém Aguardando Pagamento", TesteRegrasNegocio::fa3Recusa);
        executar("FA3.6: desistência encerra sem ativação", TesteRegrasNegocio::fa3Desistencia);
        executar("Não permite pular a validação do celular", TesteRegrasNegocio::exigeValidacao);
        executar("Endereço valida CEP", TesteRegrasNegocio::enderecoInvalido);
        System.out.printf("%nResultado: %d passaram, %d falharam%n", ok, falhas);
        if (falhas > 0) System.exit(1);
    }

    // ---------- infraestrutura mínima ----------
    private static ServicoSMSSimulado sms;

    private static AssinarPlanoController novo() {
        sms = new ServicoSMSSimulado(false);
        return new AssinarPlanoController(sms, new OperadoraCartaoSimulada(), DadosIniciais.criarPlanos(),
                new RepositorioAssinantes(), new RepositorioAssinaturas(), DadosIniciais.criarCardapio(),
                new GeradorProtocolo(), new Random(42));
    }

    private static AssinarPlanoController ateEndereco() {
        AssinarPlanoController c = novo();
        c.informarCelular("11987654321");
        c.validarCodigo(sms.getUltimoCodigoEnviado());
        c.selecionarPlano(1);
        c.registrarPreferencias(EnumSet.of(PreferenciaAlimentar.TRADICIONAL));
        c.adicionarAoPedido("P1", 5);
        c.informarEndereco(new Endereco("Rua A", "10", "", "Centro", "São Paulo", "SP", "01000-000"));
        return c;
    }

    private static String errado() { return sms.getUltimoCodigoEnviado().equals("999999") ? "000000" : "999999"; }

    // ---------- casos ----------
    private static void fluxoPrincipal() {
        AssinarPlanoController c = ateEndereco();
        verificar(c.getStatusAssinatura() == StatusAssinatura.AGUARDANDO_PAGAMENTO, "status após endereço");
        ConfirmacaoAssinatura conf = c.pagar("4111111111111111", "Fulano", "12/30", "123");
        verificar(c.getStatusAssinatura() == StatusAssinatura.ATIVA, "assinatura ativa");
        verificar(conf.getStatusPedido().equals(StatusPedido.APROVADO.getDescricao()), "pedido aprovado");
        verificar(conf.getProtocolo().startsWith("MR-"), "protocolo gerado");
        verificar(conf.getPrevisaoPrimeiraEntrega() != null, "previsão de entrega");
    }

    private static void fa1CodigoInvalido() {
        AssinarPlanoController c = novo();
        c.informarCelular("11987654321");
        verificar(!c.validarCodigo(errado()), "código errado rejeitado");
        verificar(c.getTentativasRestantes() == 2, "restam 2 tentativas");
        verificar(c.validarCodigo(sms.getUltimoCodigoEnviado()), "retentativa com código certo");
    }

    private static void fa1TresTentativas() {
        AssinarPlanoController c = novo();
        c.informarCelular("11987654321");
        c.validarCodigo(errado());
        c.validarCodigo(errado());
        esperarExcecao(TentativasEsgotadasException.class, () -> c.validarCodigo(errado()));
        esperarExcecao(TentativasEsgotadasException.class, () -> c.validarCodigo(sms.getUltimoCodigoEnviado()));
        c.solicitarNovoCodigo();
        verificar(c.validarCodigo(sms.getUltimoCodigoEnviado()), "novo código aceito");
    }

    private static void fa2Limite() {
        AssinarPlanoController c = novo();
        c.informarCelular("11987654321");
        c.validarCodigo(sms.getUltimoCodigoEnviado());
        c.selecionarPlano(1); // 5 refeições
        c.registrarPreferencias(EnumSet.of(PreferenciaAlimentar.TRADICIONAL));
        c.adicionarAoPedido("P1", 4);
        esperarExcecao(LimiteRefeicoesExcedidoException.class, () -> c.adicionarAoPedido("P2", 2));
        verificar(c.getQuantidadeDisponivel(CategoriaItem.PRATO_PRINCIPAL) == 1, "seleção anterior preservada");
        c.adicionarAoPedido("P2", 1);
        verificar(c.getQuantidadeDisponivel(CategoriaItem.PRATO_PRINCIPAL) == 0, "ajuste aceito");
    }

    private static void filtroPreferencias() {
        AssinarPlanoController c = novo();
        c.informarCelular("11987654321");
        c.validarCodigo(sms.getUltimoCodigoEnviado());
        c.selecionarPlano(1);
        c.registrarPreferencias(EnumSet.of(PreferenciaAlimentar.VEGETARIANA, PreferenciaAlimentar.SEM_LACTOSE));
        verificar(c.listarItens(CategoriaItem.PRATO_PRINCIPAL).size() == 1, "apenas o curry atende");
        esperarExcecao(RegraNegocioException.class, () -> c.adicionarAoPedido("P2", 1));
    }

    private static void fa3Recusa() {
        AssinarPlanoController c = ateEndereco();
        esperarExcecao(PagamentoNaoAutorizadoException.class,
                () -> c.pagar("5555444433330000", "Fulano", "12/30", "123"));
        verificar(c.getStatusAssinatura() == StatusAssinatura.AGUARDANDO_PAGAMENTO, "permanece aguardando");
        c.pagar("5555444433332222", "Fulano", "12/30", "123");
        verificar(c.getStatusAssinatura() == StatusAssinatura.ATIVA, "ativa após novo cartão");
    }

    private static void fa3Desistencia() {
        AssinarPlanoController c = ateEndereco();
        esperarExcecao(PagamentoNaoAutorizadoException.class,
                () -> c.pagar("5555444433330000", "Fulano", "12/30", "123"));
        c.desistirDaAssinatura();
        verificar(c.getStatusAssinatura() == StatusAssinatura.ENCERRADA_SEM_ATIVACAO, "encerrada");
    }

    private static void exigeValidacao() {
        AssinarPlanoController c = novo();
        c.informarCelular("11987654321");
        esperarExcecao(RegraNegocioException.class, c::listarPlanos);
    }

    private static void enderecoInvalido() {
        esperarExcecao(RegraNegocioException.class,
                () -> new Endereco("Rua A", "1", "", "Centro", "SP", "SP", "123"));
    }

    // ---------- asserts ----------
    private static void executar(String nome, Runnable teste) {
        try {
            teste.run();
            ok++;
            System.out.println("[OK]    " + nome);
        } catch (Throwable t) {
            falhas++;
            System.out.println("[FALHA] " + nome + " -> " + t);
        }
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) throw new AssertionError(descricao);
    }

    private static void esperarExcecao(Class<? extends Throwable> tipo, Runnable acao) {
        try {
            acao.run();
        } catch (Throwable t) {
            if (tipo.isInstance(t)) return;
            throw new AssertionError("esperava " + tipo.getSimpleName() + " mas ocorreu " + t);
        }
        throw new AssertionError("esperava " + tipo.getSimpleName());
    }
}
