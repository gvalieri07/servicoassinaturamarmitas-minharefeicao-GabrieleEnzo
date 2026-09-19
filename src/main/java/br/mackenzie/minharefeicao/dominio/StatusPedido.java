package br.mackenzie.minharefeicao.dominio;

public enum StatusPedido {
    EM_ELABORACAO("Em elaboração"),
    APROVADO("Aprovado"),
    CANCELADO("Cancelado");

    private final String descricao;
    StatusPedido(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
