package br.mackenzie.minharefeicao.dominio;

import java.util.Set;

public class PratoPrincipal extends ItemCardapio {
    private final int pesoGramas;

    public PratoPrincipal(String codigo, String nome, int pesoGramas, Set<PreferenciaAlimentar> caracteristicas) {
        super(codigo, nome, caracteristicas);
        this.pesoGramas = pesoGramas;
    }

    @Override
    public CategoriaItem getCategoria() { return CategoriaItem.PRATO_PRINCIPAL; }

    public int getPesoGramas() { return pesoGramas; }
}
