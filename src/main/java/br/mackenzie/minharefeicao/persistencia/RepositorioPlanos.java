package br.mackenzie.minharefeicao.persistencia;

import br.mackenzie.minharefeicao.dominio.PlanoAssinatura;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Repositório em memória dos planos de assinatura disponíveis. */
public class RepositorioPlanos {
    private final List<PlanoAssinatura> planos = new ArrayList<>();

    public void salvar(PlanoAssinatura plano) { planos.add(plano); }

    public List<PlanoAssinatura> listarDisponiveis() { return Collections.unmodifiableList(planos); }

    public Optional<PlanoAssinatura> buscarPorId(int id) {
        return planos.stream().filter(p -> p.getId() == id).findFirst();
    }
}
