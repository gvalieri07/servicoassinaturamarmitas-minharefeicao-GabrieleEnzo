package br.mackenzie.minharefeicao.ui;

import br.mackenzie.minharefeicao.controle.AssinarPlanoController;
import br.mackenzie.minharefeicao.controle.ConfirmacaoAssinatura;
import br.mackenzie.minharefeicao.dominio.*;
import br.mackenzie.minharefeicao.servico.ServicoSMSSimulado;

import java.util.EnumSet;
import java.util.function.Function;

/**
 * Executa automaticamente o fluxo principal e os fluxos alternativos
 * (FA1, FA2, FA3) — útil para a demonstração no vídeo.
 */
public class DemonstracaoCenarios {
    private final Function<ServicoSMSSimulado, AssinarPlanoController> fabrica;

    public DemonstracaoCenarios(Function<ServicoSMSSimulado, AssinarPlanoController> fabrica) {
        this.fabrica = fabrica;
    }

    public void executarTodos() {
        fluxoPrincipal();
        fa1CodigoInvalido();
        fa2LimiteRefeicoes();
        fa3PagamentoNaoAutorizado();
        fa3Desistencia();
    }

    private static void passo(String texto) { System.out.println("  > " + texto); }

    private AssinarPlanoController novaSessao(ServicoSMSSimulado sms) { return fabrica.apply(sms); }

    private void identificar(AssinarPlanoController c, ServicoSMSSimulado sms) {
        passo("Assinante informa o celular 11987654321");
        c.informarCelular("11987654321");
        passo("Assinante informa o código recebido: " + sms.getUltimoCodigoEnviado());
        passo("Código válido? " + c.validarCodigo(sms.getUltimoCodigoEnviado()));
    }

    private void montarPedidoPadrao(AssinarPlanoController c) {
        passo("Planos disponíveis:");
        for (PlanoAssinatura p : c.listarPlanos()) {
            System.out.printf("      [%d] %s - %d refeições - %s - %s%n", p.getId(), p.getNome(),
                    p.getQuantidadeRefeicoes(), p.getPeriodicidade().getDescricao(), Formatador.moeda(p.getValor()));
        }
        passo("Seleciona o plano 1 (Essencial, 5 refeições semanais)");
        c.selecionarPlano(1);
        passo("Registra preferências: Vegetariana + Sem lactose");
        c.registrarPreferencias(EnumSet.of(PreferenciaAlimentar.VEGETARIANA, PreferenciaAlimentar.SEM_LACTOSE));
        passo("Pratos compatíveis: " + nomes(c, CategoriaItem.PRATO_PRINCIPAL));
        passo("Seleciona 5x P4 (limite do plano: 5)");
        c.adicionarAoPedido("P4", 5);
        passo("Acompanhamentos compatíveis: " + nomes(c, CategoriaItem.ACOMPANHAMENTO));
        c.adicionarAoPedido("A1", 3);
        c.adicionarAoPedido("A3", 2);
        passo("Sobremesas compatíveis: " + nomes(c, CategoriaItem.SOBREMESA));
        c.adicionarAoPedido("S2", 2);
        c.adicionarAoPedido("S3", 1);
        passo("Composição do pedido: " + c.obterPedido().getItens());
        passo("Informa e confirma o endereço");
        c.informarEndereco(new Endereco("Rua da Consolação", "930", "Prédio 31", "Consolação",
                "São Paulo", "SP", "01302-907"));
        passo("Status da assinatura: " + c.getStatusAssinatura().getDescricao());
        passo("Valor total: " + Formatador.moeda(c.obterValorTotal()));
    }

    private static String nomes(AssinarPlanoController c, CategoriaItem cat) {
        StringBuilder sb = new StringBuilder();
        for (ItemCardapio i : c.listarItens(cat)) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(i.getCodigo()).append("-").append(i.getNome());
        }
        return sb.toString();
    }

    private void fluxoPrincipal() {
        TelaAssinatura.titulo("CENÁRIO 1 — FLUXO PRINCIPAL");
        ServicoSMSSimulado sms = new ServicoSMSSimulado(true);
        AssinarPlanoController c = novaSessao(sms);
        identificar(c, sms);
        montarPedidoPadrao(c);
        passo("Informa cartão 4111 1111 1111 1111");
        ConfirmacaoAssinatura conf = c.pagar("4111111111111111", "Gabriel V Santos", "12/30", "123");
        TelaAssinatura.exibirConfirmacao(conf);
    }

    private void fa1CodigoInvalido() {
        TelaAssinatura.titulo("CENÁRIO 2 — FA1: CÓDIGO DE CONFIRMAÇÃO INVÁLIDO");
        ServicoSMSSimulado sms = new ServicoSMSSimulado(true);
        AssinarPlanoController c = novaSessao(sms);
        c.informarCelular("(11) 91234-5678");
        for (int i = 1; i <= 3; i++) {
            try {
                boolean ok = c.validarCodigo("000000".equals(sms.getUltimoCodigoEnviado()) ? "111111" : "000000");
                passo("Tentativa " + i + " com código errado -> válido? " + ok
                        + " | tentativas restantes: " + c.getTentativasRestantes());
            } catch (TentativasEsgotadasException e) {
                passo("Tentativa " + i + " -> " + e.getMessage());
            }
        }
        passo("Assinante solicita novo código");
        c.solicitarNovoCodigo();
        passo("Informa o novo código correto -> válido? " + c.validarCodigo(sms.getUltimoCodigoEnviado()));
        passo("Fluxo retorna ao passo 5: " + c.listarPlanos().size() + " planos apresentados");
    }

    private void fa2LimiteRefeicoes() {
        TelaAssinatura.titulo("CENÁRIO 3 — FA2: QUANTIDADE INCOMPATÍVEL COM O PLANO");
        ServicoSMSSimulado sms = new ServicoSMSSimulado(false);
        AssinarPlanoController c = novaSessao(sms);
        c.informarCelular("11955554444");
        c.validarCodigo(sms.getUltimoCodigoEnviado());
        c.selecionarPlano(1);
        c.registrarPreferencias(EnumSet.of(PreferenciaAlimentar.TRADICIONAL));
        passo("Plano Essencial permite 5 pratos principais. Seleciona 4x P1");
        c.adicionarAoPedido("P1", 4);
        try {
            passo("Tenta adicionar mais 3x P2 (total 7)");
            c.adicionarAoPedido("P2", 3);
        } catch (LimiteRefeicoesExcedidoException e) {
            passo("Sistema: " + e.getMessage());
        }
        passo("Assinante ajusta a seleção para 1x P2");
        c.adicionarAoPedido("P2", 1);
        passo("Pedido: " + c.obterPedido().getItens() + " | disponível: "
                + c.getQuantidadeDisponivel(CategoriaItem.PRATO_PRINCIPAL));
    }

    private AssinarPlanoController prepararAtePagamento() {
        ServicoSMSSimulado sms = new ServicoSMSSimulado(false);
        AssinarPlanoController c = novaSessao(sms);
        c.informarCelular("11933332222");
        c.validarCodigo(sms.getUltimoCodigoEnviado());
        c.selecionarPlano(2);
        c.registrarPreferencias(EnumSet.of(PreferenciaAlimentar.TRADICIONAL));
        c.adicionarAoPedido("P2", 6);
        c.adicionarAoPedido("P5", 4);
        c.adicionarAoPedido("A1", 10);
        c.informarEndereco(new Endereco("Av. Paulista", "1000", "", "Bela Vista", "São Paulo", "SP", "01310100"));
        passo("Pedido montado (plano Equilíbrio). Status: " + c.getStatusAssinatura().getDescricao());
        return c;
    }

    private void fa3PagamentoNaoAutorizado() {
        TelaAssinatura.titulo("CENÁRIO 4 — FA3: PAGAMENTO NÃO AUTORIZADO E NOVA TENTATIVA");
        AssinarPlanoController c = prepararAtePagamento();
        try {
            passo("Informa cartão 5555 4444 3333 0000 (recusado pela operadora simulada)");
            c.pagar("5555444433330000", "Enzo B Nichimura", "10/29", "321");
        } catch (RegraNegocioException e) {
            passo("Sistema: " + e.getMessage());
            passo("Status da assinatura: " + c.getStatusAssinatura().getDescricao());
        }
        passo("Informa outro cartão 5555 4444 3333 2222");
        ConfirmacaoAssinatura conf = c.pagar("5555444433332222", "Enzo B Nichimura", "10/29", "321");
        passo("Pagamento aprovado — protocolo " + conf.getProtocolo() + " | status " + conf.getStatusAssinatura());
    }

    private void fa3Desistencia() {
        TelaAssinatura.titulo("CENÁRIO 5 — FA3.6: ASSINANTE DESISTE APÓS RECUSA");
        AssinarPlanoController c = prepararAtePagamento();
        try {
            c.pagar("4000000000000000", "Enzo B Nichimura", "10/29", "321");
        } catch (RegraNegocioException e) {
            passo("Sistema: " + e.getMessage());
        }
        passo("Assinante não deseja tentar novamente");
        c.desistirDaAssinatura();
        passo("Status final: " + c.getStatusAssinatura().getDescricao());
    }
}
