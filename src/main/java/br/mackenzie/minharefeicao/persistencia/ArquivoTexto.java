package br.mackenzie.minharefeicao.persistencia;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure Fabrication da camada de persistência: lê e grava registros em arquivos
 * de texto (.txt), um registro por linha, campos separados por "|" e codificação
 * UTF-8. Nenhuma classe de domínio conhece esta classe — quem a utiliza são os
 * repositórios, que traduzem objeto <-> registro.
 *
 * <p>Formato de um registro:
 * <pre>campo1|campo2|campo3</pre>
 * Os caracteres "|", "\" e quebras de linha são escapados com barra invertida,
 * de modo que qualquer texto digitado pelo assinante possa ser gravado.</p>
 */
public final class ArquivoTexto {

    public static final char SEPARADOR = '|';

    private final Path arquivo;

    public ArquivoTexto(Path diretorio, String nomeArquivo) {
        try {
            Files.createDirectories(diretorio);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar a pasta de dados: " + diretorio, e);
        }
        this.arquivo = diretorio.resolve(nomeArquivo);
    }

    /** Lê todos os registros do arquivo; devolve lista vazia se ele ainda não existe. */
    public List<String[]> lerRegistros() {
        List<String[]> registros = new ArrayList<>();
        if (!Files.exists(arquivo)) {
            return registros;
        }
        try {
            for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
                if (!linha.isBlank() && !linha.startsWith("#")) {
                    registros.add(separar(linha));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler " + arquivo, e);
        }
        return registros;
    }

    /** Regrava o arquivo inteiro com os registros informados. */
    public void gravarRegistros(List<String[]> registros) {
        List<String> linhas = new ArrayList<>();
        for (String[] campos : registros) {
            linhas.add(juntar(campos));
        }
        try {
            Files.write(arquivo, linhas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar " + arquivo, e);
        }
    }

    public Path getArquivo() {
        return arquivo;
    }

    // ---------- conversão registro <-> linha ----------

    static String juntar(String[] campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                sb.append(SEPARADOR);
            }
            sb.append(escapar(campos[i]));
        }
        return sb.toString();
    }

    static String[] separar(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean escapando = false;
        for (char c : linha.toCharArray()) {
            if (escapando) {
                atual.append(c == 'n' ? '\n' : c);
                escapando = false;
            } else if (c == '\\') {
                escapando = true;
            } else if (c == SEPARADOR) {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos.toArray(new String[0]);
    }

    private static String escapar(String campo) {
        String valor = campo == null ? "" : campo;
        StringBuilder sb = new StringBuilder();
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case SEPARADOR -> sb.append("\\|");
                case '\n' -> sb.append("\\n");
                case '\r' -> { /* descartado */ }
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
