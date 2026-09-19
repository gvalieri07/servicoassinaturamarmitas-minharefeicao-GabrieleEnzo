package br.mackenzie.minharefeicao.dominio;

public enum StatusAssinatura {
    EM_ELABORACAO("Em elaboração"),
    AGUARDANDO_PAGAMENTO("Aguardando Pagamento"),
    ATIVA("Ativa"),
    ENCERRADA_SEM_ATIVACAO("Encerrada sem ativação");

    private final String descricao;
    StatusAssinatura(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
