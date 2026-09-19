package br.mackenzie.minharefeicao.dominio;

/**
 * Preferência alimentar do assinante. TRADICIONAL não impõe restrição;
 * VEGETARIANA e SEM_LACTOSE são restrições que os pratos precisam atender.
 */
public enum PreferenciaAlimentar {
    TRADICIONAL("Tradicional", false),
    VEGETARIANA("Vegetariana", true),
    SEM_LACTOSE("Sem lactose", true);

    private final String descricao;
    private final boolean restricao;

    PreferenciaAlimentar(String descricao, boolean restricao) {
        this.descricao = descricao;
        this.restricao = restricao;
    }

    public String getDescricao() { return descricao; }
    public boolean isRestricao() { return restricao; }
}
