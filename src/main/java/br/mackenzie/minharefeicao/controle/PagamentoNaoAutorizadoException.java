package br.mackenzie.minharefeicao.controle;

import br.mackenzie.minharefeicao.dominio.RegraNegocioException;

/** FA3 – A Operadora de Cartão de Crédito não autorizou o pagamento. */
public class PagamentoNaoAutorizadoException extends RegraNegocioException {
    private static final long serialVersionUID = 1L;

    public PagamentoNaoAutorizadoException(String motivo) {
        super("Pagamento não aprovado: " + motivo
                + " A assinatura permanece Aguardando Pagamento. Informe outro cartão ou tente novamente.");
    }
}
