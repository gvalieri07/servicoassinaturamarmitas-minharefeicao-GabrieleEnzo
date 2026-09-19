package br.mackenzie.minharefeicao.dominio;

/** Exceção base para violações de regras de negócio do caso de uso. */
public class RegraNegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) { super(mensagem); }
}
