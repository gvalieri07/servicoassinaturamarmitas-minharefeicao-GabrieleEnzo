package br.mackenzie.minharefeicao.dominio;

/** FA2 – Quantidade de refeições incompatível com o plano. */
public class LimiteRefeicoesExcedidoException extends RegraNegocioException {
    private static final long serialVersionUID = 1L;

    private final int limite;
    private final int jaSelecionado;

    public LimiteRefeicoesExcedidoException(CategoriaItem categoria, int limite, int jaSelecionado) {
        super(String.format("Limite do plano excedido para %s: máximo de %d, já selecionado(s) %d. Ajuste sua seleção.",
                categoria.getDescricao().toLowerCase(), limite, jaSelecionado));
        this.limite = limite;
        this.jaSelecionado = jaSelecionado;
    }

    public int getLimite() { return limite; }
    public int getDisponivel() { return limite - jaSelecionado; }
}
