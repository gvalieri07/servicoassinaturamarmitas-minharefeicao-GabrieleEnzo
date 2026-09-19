package br.mackenzie.minharefeicao.dominio;

/** Periodicidade de entrega de um plano de assinatura. */
public enum Periodicidade {
    SEMANAL("Semanal", 7),
    QUINZENAL("Quinzenal", 14),
    MENSAL("Mensal", 30);

    private final String descricao;
    private final int intervaloEmDias;

    Periodicidade(String descricao, int intervaloEmDias) {
        this.descricao = descricao;
        this.intervaloEmDias = intervaloEmDias;
    }

    public String getDescricao() { return descricao; }
    public int getIntervaloEmDias() { return intervaloEmDias; }
}
