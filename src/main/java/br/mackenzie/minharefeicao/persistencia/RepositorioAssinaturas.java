package br.mackenzie.minharefeicao.persistencia;

import br.mackenzie.minharefeicao.dominio.Assinatura;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Repositório em memória de assinaturas. */
public class RepositorioAssinaturas {
    private final List<Assinatura> assinaturas = new ArrayList<>();

    public void salvar(Assinatura assinatura) {
        if (!assinaturas.contains(assinatura)) {
            assinaturas.add(assinatura);
        }
    }

    public List<Assinatura> listarTodas() { return Collections.unmodifiableList(assinaturas); }
}
