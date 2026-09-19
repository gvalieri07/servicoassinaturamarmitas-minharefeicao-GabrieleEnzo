package br.mackenzie.minharefeicao.dominio;

import java.util.Set;

public class Acompanhamento extends ItemCardapio {
    public Acompanhamento(String codigo, String nome, Set<PreferenciaAlimentar> caracteristicas) {
        super(codigo, nome, caracteristicas);
    }

    @Override
    public CategoriaItem getCategoria() { return CategoriaItem.ACOMPANHAMENTO; }
}
