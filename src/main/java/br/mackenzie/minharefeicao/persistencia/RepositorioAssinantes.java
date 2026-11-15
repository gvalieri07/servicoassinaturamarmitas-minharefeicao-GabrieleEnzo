package br.mackenzie.minharefeicao.persistencia;

import br.mackenzie.minharefeicao.dominio.Assinante;
import br.mackenzie.minharefeicao.dominio.Endereco;
import br.mackenzie.minharefeicao.dominio.PreferenciaAlimentar;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Repositório de assinantes (chave: celular).
 *
 * <p>Pode trabalhar somente em memória (construtor sem argumentos, usado nos
 * testes e na demonstração) ou com persistência em arquivo de texto
 * {@code assinantes.txt}, carregado na criação e regravado a cada
 * {@link #salvar(Assinante)}.</p>
 *
 * <p>Registro gravado:
 * {@code celular|celularVerificado|preferencias|logradouro|numero|complemento|bairro|cidade|uf|cep}</p>
 */
public class RepositorioAssinantes {

    private static final String ARQUIVO = "assinantes.txt";

    private final Map<String, Assinante> porCelular = new LinkedHashMap<>();
    private final ArquivoTexto arquivo;

    /** Repositório apenas em memória. */
    public RepositorioAssinantes() {
        this.arquivo = null;
    }

    /** Repositório persistente: carrega os assinantes já gravados na pasta informada. */
    public RepositorioAssinantes(Path diretorioDados) {
        this.arquivo = new ArquivoTexto(diretorioDados, ARQUIVO);
        carregar();
    }

    public void salvar(Assinante assinante) {
        porCelular.put(assinante.getCelular(), assinante);
        gravar();
    }

    public Optional<Assinante> buscarPorCelular(String celular) {
        return Optional.ofNullable(porCelular.get(celular));
    }

    public Collection<Assinante> listarTodos() {
        return porCelular.values();
    }

    public boolean isPersistente() {
        return arquivo != null;
    }

    public Path getArquivo() {
        return arquivo == null ? null : arquivo.getArquivo();
    }

    // ---------- objeto <-> registro ----------

    private void carregar() {
        for (String[] campos : arquivo.lerRegistros()) {
            if (campos.length < 10) {
                continue;
            }
            Assinante assinante = new Assinante(campos[0]);
            if (Boolean.parseBoolean(campos[1])) {
                assinante.confirmarCelular();
            }
            Set<PreferenciaAlimentar> preferencias = lerPreferencias(campos[2]);
            if (!preferencias.isEmpty()) {
                assinante.registrarPreferencias(preferencias);
            }
            if (!campos[3].isBlank()) {
                assinante.definirEnderecoEntrega(new Endereco(campos[3], campos[4], campos[5],
                        campos[6], campos[7], campos[8], campos[9]));
            }
            porCelular.put(assinante.getCelular(), assinante);
        }
    }

    private void gravar() {
        if (arquivo == null) {
            return;
        }
        List<String[]> registros = new ArrayList<>();
        for (Assinante a : porCelular.values()) {
            Endereco e = a.getEnderecoEntrega();
            registros.add(new String[] {
                    a.getCelular(),
                    String.valueOf(a.isCelularVerificado()),
                    escreverPreferencias(a.getPreferencias()),
                    e == null ? "" : e.getLogradouro(),
                    e == null ? "" : e.getNumero(),
                    e == null ? "" : e.getComplemento(),
                    e == null ? "" : e.getBairro(),
                    e == null ? "" : e.getCidade(),
                    e == null ? "" : e.getUf(),
                    e == null ? "" : e.getCep()
            });
        }
        arquivo.gravarRegistros(registros);
    }

    private static Set<PreferenciaAlimentar> lerPreferencias(String campo) {
        Set<PreferenciaAlimentar> preferencias = EnumSet.noneOf(PreferenciaAlimentar.class);
        for (String nome : campo.split(",")) {
            if (!nome.isBlank()) {
                preferencias.add(PreferenciaAlimentar.valueOf(nome.trim()));
            }
        }
        return preferencias;
    }

    private static String escreverPreferencias(Set<PreferenciaAlimentar> preferencias) {
        StringBuilder sb = new StringBuilder();
        for (PreferenciaAlimentar p : preferencias) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(p.name());
        }
        return sb.toString();
    }
}
