package br.mackenzie.minharefeicao.dominio;

/** Categorias de itens do cardápio. */
public enum CategoriaItem {
    PRATO_PRINCIPAL("Prato principal"),
    ACOMPANHAMENTO("Acompanhamento"),
    SOBREMESA("Sobremesa");

    private final String descricao;

    CategoriaItem(String descricao) { this.descricao = descricao; }

    public String getDescricao() { return descricao; }
}
