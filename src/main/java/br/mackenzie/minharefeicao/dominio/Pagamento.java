package br.mackenzie.minharefeicao.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro de uma tentativa de pagamento (aprovada ou recusada). */
public class Pagamento {
    private final BigDecimal valor;
    private final LocalDateTime dataHora;
    private final String cartaoMascarado;
    private final StatusPagamento status;
    private final String codigoAutorizacao;

    public Pagamento(BigDecimal valor, CartaoCredito cartao, boolean aprovado, String codigoAutorizacao) {
        this.valor = valor;
        this.dataHora = LocalDateTime.now();
        this.cartaoMascarado = cartao.getNumeroMascarado();
        this.status = aprovado ? StatusPagamento.APROVADO : StatusPagamento.RECUSADO;
        this.codigoAutorizacao = codigoAutorizacao;
    }

    public boolean isAprovado() { return status == StatusPagamento.APROVADO; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getCartaoMascarado() { return cartaoMascarado; }
    public StatusPagamento getStatus() { return status; }
    public String getCodigoAutorizacao() { return codigoAutorizacao; }
}
