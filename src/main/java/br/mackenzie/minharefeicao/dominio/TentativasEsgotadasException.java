package br.mackenzie.minharefeicao.dominio;

/** FA1.5 – Após três tentativas inválidas é preciso solicitar um novo código. */
public class TentativasEsgotadasException extends RegraNegocioException {
    private static final long serialVersionUID = 1L;

    public TentativasEsgotadasException() {
        super("Três tentativas inválidas. A validação foi encerrada: solicite o envio de um novo código.");
    }
}
