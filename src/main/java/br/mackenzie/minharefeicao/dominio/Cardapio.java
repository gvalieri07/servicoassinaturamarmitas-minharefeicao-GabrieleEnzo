package br.mackenzie.minharefeicao.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Cardápio vigente: agrega (composição) os itens oferecidos. */
public class Cardapio {
    private final List<ItemCardapio> itens = new ArrayList<>();

    public void adicionarItem(ItemCardapio item) { itens.add(item); }

    /** Itens disponíveis de uma categoria que atendem às preferências do assinante. */
    public List<ItemCardapio> listarDisponiveis(CategoriaItem categoria, Set<PreferenciaAlimentar> preferencias) {
        List<ItemCardapio> resultado = new ArrayList<>();
        for (ItemCardapio item : itens) {
            if (item.getCategoria() == categoria && item.isDisponivel() && item.atende(preferencias)) {
                resultado.add(item);
            }
        }
        return Collections.unmodifiableList(resultado);
    }

    public Optional<ItemCardapio> buscarPorCodigo(String codigo) {
        return itens.stream().filter(i -> i.getCodigo().equalsIgnoreCase(codigo)).findFirst();
    }
}
