package br.mackenzie.minharefeicao.servico;

import br.mackenzie.minharefeicao.dominio.CartaoCredito;
import java.math.BigDecimal;

/** Porta para o ator secundário "Operadora de Cartão de Crédito". */
public interface OperadoraCartao {
    RespostaAutorizacao autorizar(CartaoCredito cartao, BigDecimal valor);
}
