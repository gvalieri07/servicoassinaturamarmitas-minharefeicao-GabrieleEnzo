package br.mackenzie.minharefeicao.dominio;

import java.time.YearMonth;

/** Dados do cartão informados pelo assinante. O número completo nunca é exibido. */
public class CartaoCredito {
    private final String numero;
    private final String nomeTitular;
    private final YearMonth validade;
    private final String cvv;

    public CartaoCredito(String numero, String nomeTitular, String validadeMMAA, String cvv) {
        String digitos = numero == null ? "" : numero.replaceAll("\\D", "");
        if (!digitos.matches("\\d{16}")) {
            throw new RegraNegocioException("Número do cartão deve ter 16 dígitos.");
        }
        if (nomeTitular == null || nomeTitular.isBlank()) {
            throw new RegraNegocioException("Nome do titular é obrigatório.");
        }
        this.validade = interpretarValidade(validadeMMAA);
        if (validade.isBefore(YearMonth.now())) {
            throw new RegraNegocioException("Cartão vencido.");
        }
        if (cvv == null || !cvv.matches("\\d{3}")) {
            throw new RegraNegocioException("CVV deve ter 3 dígitos.");
        }
        this.numero = digitos;
        this.nomeTitular = nomeTitular.trim().toUpperCase();
        this.cvv = cvv;
    }

    private static YearMonth interpretarValidade(String mmaa) {
        if (mmaa == null || !mmaa.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new RegraNegocioException("Validade deve estar no formato MM/AA.");
        }
        int mes = Integer.parseInt(mmaa.substring(0, 2));
        int ano = 2000 + Integer.parseInt(mmaa.substring(3));
        return YearMonth.of(ano, mes);
    }

    /** Usado apenas pelo serviço da operadora para autorização. */
    public String getNumero() { return numero; }
    public String getCvv() { return cvv; }
    public String getNomeTitular() { return nomeTitular; }
    public YearMonth getValidade() { return validade; }

    public String getNumeroMascarado() { return "**** **** **** " + numero.substring(12); }
}
