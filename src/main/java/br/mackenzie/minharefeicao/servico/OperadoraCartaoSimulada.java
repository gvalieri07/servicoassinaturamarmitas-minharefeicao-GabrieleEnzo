package br.mackenzie.minharefeicao.servico;

import br.mackenzie.minharefeicao.dominio.CartaoCredito;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Operadora simulada. Regra de teste: cartões terminados em 0000 são recusados
 * (usado para demonstrar o FA3); os demais são aprovados.
 */
public class OperadoraCartaoSimulada implements OperadoraCartao {
    private final AtomicInteger sequencia = new AtomicInteger(1000);

    @Override
    public RespostaAutorizacao autorizar(CartaoCredito cartao, BigDecimal valor) {
        if (cartao.getNumero().endsWith("0000")) {
            return new RespostaAutorizacao(false, null, "Transação não autorizada pela operadora.");
        }
        return new RespostaAutorizacao(true, "AUT" + sequencia.incrementAndGet(), "Transação aprovada.");
    }
}
