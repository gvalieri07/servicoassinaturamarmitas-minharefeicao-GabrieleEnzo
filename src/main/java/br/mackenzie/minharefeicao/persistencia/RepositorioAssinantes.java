package br.mackenzie.minharefeicao.persistencia;

import br.mackenzie.minharefeicao.dominio.Assinante;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Repositório em memória de assinantes (chave: celular). */
public class RepositorioAssinantes {
    private final Map<String, Assinante> porCelular = new HashMap<>();

    public void salvar(Assinante assinante) { porCelular.put(assinante.getCelular(), assinante); }

    public Optional<Assinante> buscarPorCelular(String celular) {
        return Optional.ofNullable(porCelular.get(celular));
    }
}
