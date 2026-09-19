package br.mackenzie.minharefeicao.dominio;

/** Objeto de valor imutável que representa o endereço de entrega. */
public final class Endereco {
    private final String logradouro;
    private final String numero;
    private final String complemento;
    private final String bairro;
    private final String cidade;
    private final String uf;
    private final String cep;

    public Endereco(String logradouro, String numero, String complemento,
                    String bairro, String cidade, String uf, String cep) {
        this.logradouro = obrigatorio(logradouro, "Logradouro");
        this.numero = obrigatorio(numero, "Número");
        this.complemento = complemento == null ? "" : complemento.trim();
        this.bairro = obrigatorio(bairro, "Bairro");
        this.cidade = obrigatorio(cidade, "Cidade");
        String ufNormalizada = obrigatorio(uf, "UF").toUpperCase();
        if (!ufNormalizada.matches("[A-Z]{2}")) {
            throw new RegraNegocioException("UF deve ter 2 letras.");
        }
        this.uf = ufNormalizada;
        String cepDigitos = obrigatorio(cep, "CEP").replaceAll("\\D", "");
        if (cepDigitos.length() != 8) {
            throw new RegraNegocioException("CEP deve conter 8 dígitos.");
        }
        this.cep = cepDigitos;
    }

    private static String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException(campo + " é obrigatório.");
        }
        return valor.trim();
    }

    public String getCidade() { return cidade; }
    public String getCep() { return cep; }

    @Override
    public String toString() {
        String compl = complemento.isEmpty() ? "" : " - " + complemento;
        return String.format("%s, %s%s - %s, %s/%s - CEP %s-%s",
                logradouro, numero, compl, bairro, cidade, uf, cep.substring(0, 5), cep.substring(5));
    }
}
