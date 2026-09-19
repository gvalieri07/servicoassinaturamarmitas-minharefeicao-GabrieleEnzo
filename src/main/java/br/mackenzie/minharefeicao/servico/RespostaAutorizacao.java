package br.mackenzie.minharefeicao.servico;

/** Resposta da Operadora de Cartão de Crédito a um pedido de autorização. */
public class RespostaAutorizacao {
    private final boolean aprovado;
    private final String codigoAutorizacao;
    private final String mensagem;

    public RespostaAutorizacao(boolean aprovado, String codigoAutorizacao, String mensagem) {
        this.aprovado = aprovado;
        this.codigoAutorizacao = codigoAutorizacao;
        this.mensagem = mensagem;
    }

    public boolean isAprovado() { return aprovado; }
    public String getCodigoAutorizacao() { return codigoAutorizacao; }
    public String getMensagem() { return mensagem; }
}
