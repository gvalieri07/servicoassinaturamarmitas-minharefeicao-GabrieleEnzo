package br.mackenzie.minharefeicao.controle;

import br.mackenzie.minharefeicao.dominio.Assinatura;
import br.mackenzie.minharefeicao.dominio.ItemPedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** DTO imutável com os dados da mensagem de confirmação (passo 31). */
public final class ConfirmacaoAssinatura {
    private final String protocolo;
    private final String plano;
    private final String periodicidade;
    private final List<String> refeicoes;
    private final String enderecoEntrega;
    private final LocalDate previsaoPrimeiraEntrega;
    private final BigDecimal valorTotal;
    private final String statusAssinatura;
    private final String statusPedido;

    private ConfirmacaoAssinatura(Assinatura a) {
        this.protocolo = a.getProtocolo();
        this.plano = a.getPlano().getNome() + " (" + a.getPlano().getQuantidadeRefeicoes() + " refeições)";
        this.periodicidade = a.getPlano().getPeriodicidade().getDescricao();
        List<String> itens = new ArrayList<>();
        for (ItemPedido ip : a.getPedido().getItens()) {
            itens.add(ip.getCategoria().getDescricao() + ": " + ip);
        }
        this.refeicoes = Collections.unmodifiableList(itens);
        this.enderecoEntrega = a.getAssinante().getEnderecoEntrega().toString();
        this.previsaoPrimeiraEntrega = a.getPrevisaoPrimeiraEntrega();
        this.valorTotal = a.calcularValorTotal();
        this.statusAssinatura = a.getStatus().getDescricao();
        this.statusPedido = a.getPedido().getStatus().getDescricao();
    }

    static ConfirmacaoAssinatura de(Assinatura assinatura) { return new ConfirmacaoAssinatura(assinatura); }

    public String getProtocolo() { return protocolo; }
    public String getPlano() { return plano; }
    public String getPeriodicidade() { return periodicidade; }
    public List<String> getRefeicoes() { return refeicoes; }
    public String getEnderecoEntrega() { return enderecoEntrega; }
    public LocalDate getPrevisaoPrimeiraEntrega() { return previsaoPrimeiraEntrega; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public String getStatusAssinatura() { return statusAssinatura; }
    public String getStatusPedido() { return statusPedido; }
}
