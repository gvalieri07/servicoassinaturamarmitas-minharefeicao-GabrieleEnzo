package br.mackenzie.minharefeicao.dominio;

import java.util.Set;

public class Sobremesa extends ItemCardapio {
    public Sobremesa(String codigo, String nome, Set<PreferenciaAlimentar> caracteristicas) {
        super(codigo, nome, caracteristicas);
    }

    @Override
    public CategoriaItem getCategoria() { return CategoriaItem.SOBREMESA; }
}
