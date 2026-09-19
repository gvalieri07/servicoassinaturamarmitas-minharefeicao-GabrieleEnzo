package br.mackenzie.minharefeicao.dominio;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Assinante da plataforma, identificado pelo número de celular. */
public class Assinante {
    private final String celular;
    private boolean celularVerificado;
    private final Set<PreferenciaAlimentar> preferencias = EnumSet.noneOf(PreferenciaAlimentar.class);
    private Endereco enderecoEntrega;

    public Assinante(String celular) {
        this.celular = normalizarCelular(celular);
    }

    public static String normalizarCelular(String celular) {
        String digitos = celular == null ? "" : celular.replaceAll("\\D", "");
        if (!digitos.matches("\\d{2}9\\d{8}")) {
            throw new RegraNegocioException("Celular inválido. Informe DDD + 9 dígitos, ex.: 11987654321.");
        }
        return digitos;
    }

    public void confirmarCelular() { this.celularVerificado = true; }

    public void registrarPreferencias(Set<PreferenciaAlimentar> novas) {
        if (novas == null || novas.isEmpty()) {
            throw new RegraNegocioException("Informe ao menos uma preferência alimentar.");
        }
        preferencias.clear();
        preferencias.addAll(novas);
    }

    public void definirEnderecoEntrega(Endereco endereco) {
        if (endereco == null) {
            throw new RegraNegocioException("Endereço de entrega é obrigatório.");
        }
        this.enderecoEntrega = endereco;
    }

    public String getCelular() { return celular; }
    public boolean isCelularVerificado() { return celularVerificado; }
    public Set<PreferenciaAlimentar> getPreferencias() { return Collections.unmodifiableSet(preferencias); }
    public Endereco getEnderecoEntrega() { return enderecoEntrega; }

    public String getCelularMascarado() {
        return String.format("(%s) *****-%s", celular.substring(0, 2), celular.substring(7));
    }
}
