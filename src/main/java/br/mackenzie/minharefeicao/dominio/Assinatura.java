package br.mackenzie.minharefeicao.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Assinatura de um plano. Controla o ciclo de vida (status) e compõe o
 * Pedido e os Pagamentos.
 */
public class Assinatura {
    public static final int DIAS_PARA_PRIMEIRA_ENTREGA = 3;

    private final Assinante assinante;
    private final PlanoAssinatura plano;
    private final Pedido pedido;
    private final List<Pagamento> pagamentos = new ArrayList<>();
    private StatusAssinatura status = StatusAssinatura.EM_ELABORACAO;
    private String protocolo;
    private LocalDate previsaoPrimeiraEntrega;

    public Assinatura(Assinante assinante, PlanoAssinatura plano) {
        if (!assinante.isCelularVerificado()) {
            throw new RegraNegocioException("Celular do assinante ainda não foi validado.");
        }
        this.assinante = assinante;
        this.plano = plano;
        this.pedido = new Pedido(plano); // Creator: Assinatura compõe o Pedido
    }

    public BigDecimal calcularValorTotal() { return plano.getValor(); }

    /** Passos 12, 15 e 18: delega ao Pedido (evita que o controlador navegue pelo grafo). */
    public void adicionarItemAoPedido(ItemCardapio item, int quantidade) {
        exigirStatus(StatusAssinatura.EM_ELABORACAO);
        pedido.adicionarItem(item, quantidade);
    }

    /** Passo 23: pedido completo + endereço definido => Aguardando Pagamento. */
    public void aguardarPagamento() {
        exigirStatus(StatusAssinatura.EM_ELABORACAO);
        if (!pedido.possuiPratoPrincipal()) {
            throw new RegraNegocioException("Selecione ao menos um prato principal.");
        }
        if (assinante.getEnderecoEntrega() == null) {
            throw new RegraNegocioException("Informe o endereço de entrega.");
        }
        status = StatusAssinatura.AGUARDANDO_PAGAMENTO;
    }

    /**
     * Passos 28-29 / FA3: registra o pagamento; se aprovado ativa a assinatura
     * e aprova o pedido, senão permanece Aguardando Pagamento.
     */
    public void registrarPagamento(Pagamento pagamento) {
        exigirStatus(StatusAssinatura.AGUARDANDO_PAGAMENTO);
        pagamentos.add(pagamento);
        if (pagamento.isAprovado()) {
            status = StatusAssinatura.ATIVA;
            pedido.aprovar();
            previsaoPrimeiraEntrega = LocalDate.now().plusDays(DIAS_PARA_PRIMEIRA_ENTREGA);
        }
    }

    public void atribuirProtocolo(String protocolo) {
        exigirStatus(StatusAssinatura.ATIVA);
        this.protocolo = protocolo;
    }

    /** FA3.6: o assinante desiste; a assinatura é encerrada sem ativação. */
    public void encerrarSemAtivacao() {
        exigirStatus(StatusAssinatura.AGUARDANDO_PAGAMENTO);
        status = StatusAssinatura.ENCERRADA_SEM_ATIVACAO;
        pedido.cancelar();
    }

    private void exigirStatus(StatusAssinatura esperado) {
        if (status != esperado) {
            throw new RegraNegocioException("Operação inválida: assinatura está " + status.getDescricao() + ".");
        }
    }

    public Assinante getAssinante() { return assinante; }
    public PlanoAssinatura getPlano() { return plano; }
    public Pedido getPedido() { return pedido; }
    public StatusAssinatura getStatus() { return status; }
    public String getProtocolo() { return protocolo; }
    public LocalDate getPrevisaoPrimeiraEntrega() { return previsaoPrimeiraEntrega; }
    public List<Pagamento> getPagamentos() { return Collections.unmodifiableList(pagamentos); }
}
