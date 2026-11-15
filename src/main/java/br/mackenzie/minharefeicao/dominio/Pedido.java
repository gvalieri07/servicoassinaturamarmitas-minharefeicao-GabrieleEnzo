package br.mackenzie.minharefeicao.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pedido com a composição das refeições. É o especialista na regra de
 * limite de refeições do plano (FA2).
 */
public class Pedido {
    private final PlanoAssinatura plano;
    private final List<ItemPedido> itens = new ArrayList<>();
    private StatusPedido status = StatusPedido.EM_ELABORACAO;

    public Pedido(PlanoAssinatura plano) { this.plano = plano; }

    public void adicionarItem(ItemCardapio item, int quantidade) {
        if (status != StatusPedido.EM_ELABORACAO) {
            throw new RegraNegocioException("O pedido não pode mais ser alterado.");
        }
        if (quantidade <= 0) {
            throw new RegraNegocioException("Quantidade deve ser maior que zero.");
        }
        CategoriaItem categoria = item.getCategoria();
        int limite = plano.getLimitePor(categoria);
        int jaSelecionado = getQuantidadeSelecionada(categoria);
        if (jaSelecionado + quantidade > limite) {
            throw new LimiteRefeicoesExcedidoException(categoria, limite, jaSelecionado);
        }
        for (ItemPedido existente : itens) {
            if (existente.getItem() == item) {
                existente.acrescentar(quantidade);
                return;
            }
        }
        itens.add(new ItemPedido(item, quantidade)); // Creator: Pedido contém ItemPedido
    }

    public int getQuantidadeSelecionada(CategoriaItem categoria) {
        int total = 0;
        for (ItemPedido ip : itens) {
            if (ip.getCategoria() == categoria) total += ip.getQuantidade();
        }
        return total;
    }

    public int getQuantidadeDisponivel(CategoriaItem categoria) {
        return plano.getLimitePor(categoria) - getQuantidadeSelecionada(categoria);
    }

    public List<ItemPedido> getItens(CategoriaItem categoria) {
        List<ItemPedido> filtrados = new ArrayList<>();
        for (ItemPedido ip : itens) {
            if (ip.getCategoria() == categoria) filtrados.add(ip);
        }
        return Collections.unmodifiableList(filtrados);
    }

    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }

    public boolean possuiPratoPrincipal() { return getQuantidadeSelecionada(CategoriaItem.PRATO_PRINCIPAL) > 0; }

    /**
     * Reconstitui um pedido já gravado em arquivo (camada de persistência).
     * Não revalida os limites do plano: os dados foram validados quando o
     * pedido foi montado, no passo 12/15/18 do cenário.
     */
    public static Pedido restaurar(PlanoAssinatura plano, StatusPedido status) {
        Pedido pedido = new Pedido(plano);
        pedido.status = status;
        return pedido;
    }

    /** Acrescenta um item lido do arquivo, sem reaplicar a regra do FA2. */
    public void restaurarItem(ItemCardapio item, int quantidade) {
        itens.add(new ItemPedido(item, quantidade));
    }

    void aprovar() { this.status = StatusPedido.APROVADO; }
    void cancelar() { this.status = StatusPedido.CANCELADO; }

    public StatusPedido getStatus() { return status; }
}
