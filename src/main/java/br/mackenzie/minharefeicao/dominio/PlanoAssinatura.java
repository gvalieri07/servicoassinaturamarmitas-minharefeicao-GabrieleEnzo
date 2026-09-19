package br.mackenzie.minharefeicao.dominio;

import java.math.BigDecimal;

/** Plano de assinatura: quantidade de refeições por entrega, periodicidade e valor. */
public class PlanoAssinatura {
    private final int id;
    private final String nome;
    private final int quantidadeRefeicoes;
    private final Periodicidade periodicidade;
    private final BigDecimal valor;

    public PlanoAssinatura(int id, String nome, int quantidadeRefeicoes,
                           Periodicidade periodicidade, BigDecimal valor) {
        if (quantidadeRefeicoes <= 0) {
            throw new IllegalArgumentException("Quantidade de refeições deve ser positiva.");
        }
        this.id = id;
        this.nome = nome;
        this.quantidadeRefeicoes = quantidadeRefeicoes;
        this.periodicidade = periodicidade;
        this.valor = valor;
    }

    /** Limite de itens por categoria: 1 item de cada categoria por refeição. */
    public int getLimitePor(CategoriaItem categoria) {
        return quantidadeRefeicoes;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public int getQuantidadeRefeicoes() { return quantidadeRefeicoes; }
    public Periodicidade getPeriodicidade() { return periodicidade; }
    public BigDecimal getValor() { return valor; }
}
