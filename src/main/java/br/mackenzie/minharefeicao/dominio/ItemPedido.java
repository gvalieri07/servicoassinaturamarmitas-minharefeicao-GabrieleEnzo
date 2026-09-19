package br.mackenzie.minharefeicao.dominio;

/** Classe associativa entre Pedido e ItemCardapio (guarda a quantidade escolhida). */
public class ItemPedido {
    private final ItemCardapio item;
    private int quantidade;

    ItemPedido(ItemCardapio item, int quantidade) {
        this.item = item;
        this.quantidade = quantidade;
    }

    void acrescentar(int qtd) { this.quantidade += qtd; }

    public ItemCardapio getItem() { return item; }
    public int getQuantidade() { return quantidade; }
    public CategoriaItem getCategoria() { return item.getCategoria(); }

    @Override
    public String toString() { return quantidade + "x " + item.getNome(); }
}
