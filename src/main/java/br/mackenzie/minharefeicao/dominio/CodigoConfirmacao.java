package br.mackenzie.minharefeicao.dominio;

import java.util.Random;

/**
 * Código de confirmação enviado por SMS. Encapsula a regra de no máximo
 * três tentativas de validação (FA1).
 */
public class CodigoConfirmacao {
    public static final int MAXIMO_TENTATIVAS = 3;

    private final String valor;
    private int tentativasInvalidas;
    private boolean validado;

    private CodigoConfirmacao(String valor) {
        this.valor = valor;
    }

    /** Creator: gera um novo código de 6 dígitos. */
    public static CodigoConfirmacao gerar(Random random) {
        return new CodigoConfirmacao(String.format("%06d", random.nextInt(1_000_000)));
    }

    /**
     * Valida o código informado.
     * @return true se válido; false se inválido e ainda há tentativas.
     * @throws TentativasEsgotadasException quando a 3ª tentativa inválida é atingida.
     */
    public boolean validar(String informado) {
        if (isBloqueado()) {
            throw new TentativasEsgotadasException();
        }
        if (valor.equals(informado == null ? "" : informado.trim())) {
            validado = true;
            return true;
        }
        tentativasInvalidas++;
        if (isBloqueado()) {
            throw new TentativasEsgotadasException();
        }
        return false;
    }

    public boolean isBloqueado() { return tentativasInvalidas >= MAXIMO_TENTATIVAS; }
    public boolean isValidado() { return validado; }
    public int getTentativasRestantes() { return MAXIMO_TENTATIVAS - tentativasInvalidas; }

    /** Texto enviado ao assinante; o valor em si não é exposto por getter. */
    public String gerarMensagemSMS() {
        return "Minha Refeição: seu código de confirmação é " + valor;
    }
}
