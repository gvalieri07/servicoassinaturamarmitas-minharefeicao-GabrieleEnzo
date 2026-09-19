package br.mackenzie.minharefeicao.dominio;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Item do cardápio (generalização de prato principal, acompanhamento e sobremesa). */
public abstract class ItemCardapio {
    private final String codigo;
    private final String nome;
    private final Set<PreferenciaAlimentar> caracteristicas;
    private boolean disponivel = true;

    protected ItemCardapio(String codigo, String nome, Set<PreferenciaAlimentar> caracteristicas) {
        this.codigo = codigo;
        this.nome = nome;
        this.caracteristicas = caracteristicas.isEmpty()
                ? EnumSet.noneOf(PreferenciaAlimentar.class)
                : EnumSet.copyOf(caracteristicas);
    }

    public abstract CategoriaItem getCategoria();

    /** Information Expert: o próprio item sabe se atende às restrições do assinante. */
    public boolean atende(Set<PreferenciaAlimentar> preferencias) {
        for (PreferenciaAlimentar p : preferencias) {
            if (p.isRestricao() && !caracteristicas.contains(p)) {
                return false;
            }
        }
        return true;
    }

    public void tornarIndisponivel() { this.disponivel = false; }

    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public boolean isDisponivel() { return disponivel; }
    public Set<PreferenciaAlimentar> getCaracteristicas() { return Collections.unmodifiableSet(caracteristicas); }
}
