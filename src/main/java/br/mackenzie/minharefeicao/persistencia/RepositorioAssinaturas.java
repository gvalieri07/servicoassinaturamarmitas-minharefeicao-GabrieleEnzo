package br.mackenzie.minharefeicao.persistencia;

import br.mackenzie.minharefeicao.dominio.Assinante;
import br.mackenzie.minharefeicao.dominio.Assinatura;
import br.mackenzie.minharefeicao.dominio.Cardapio;
import br.mackenzie.minharefeicao.dominio.ItemCardapio;
import br.mackenzie.minharefeicao.dominio.ItemPedido;
import br.mackenzie.minharefeicao.dominio.Pagamento;
import br.mackenzie.minharefeicao.dominio.Pedido;
import br.mackenzie.minharefeicao.dominio.PlanoAssinatura;
import br.mackenzie.minharefeicao.dominio.StatusAssinatura;
import br.mackenzie.minharefeicao.dominio.StatusPagamento;
import br.mackenzie.minharefeicao.dominio.StatusPedido;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositório das assinaturas contratadas.
 *
 * <p>Em memória pelo construtor sem argumentos; persistente quando recebe a
 * pasta de dados. A persistência usa três arquivos de texto relacionados pelo
 * código da assinatura:</p>
 *
 * <ul>
 *   <li>{@code assinaturas.txt} — {@code codigo|celular|idPlano|dataCriacao|statusAssinatura|statusPedido|protocolo|previsaoPrimeiraEntrega}</li>
 *   <li>{@code itens_pedido.txt} — {@code codigoAssinatura|codigoItem|quantidade}</li>
 *   <li>{@code pagamentos.txt} — {@code codigoAssinatura|dataHora|valor|cartaoMascarado|status|codigoAutorizacao}</li>
 * </ul>
 *
 * <p>Na carga, os registros voltam a ser objetos de domínio: o plano é obtido
 * no {@link RepositorioPlanos}, os itens no {@link Cardapio} e o assinante no
 * {@link RepositorioAssinantes}.</p>
 */
public class RepositorioAssinaturas {

    private static final String ARQUIVO_ASSINATURAS = "assinaturas.txt";
    private static final String ARQUIVO_ITENS = "itens_pedido.txt";
    private static final String ARQUIVO_PAGAMENTOS = "pagamentos.txt";

    private final Map<String, Assinatura> porCodigo = new LinkedHashMap<>();

    private final ArquivoTexto arquivoAssinaturas;
    private final ArquivoTexto arquivoItens;
    private final ArquivoTexto arquivoPagamentos;
    private final RepositorioAssinantes repositorioAssinantes;
    private final RepositorioPlanos repositorioPlanos;
    private final Cardapio cardapio;

    /** Repositório apenas em memória. */
    public RepositorioAssinaturas() {
        this.arquivoAssinaturas = null;
        this.arquivoItens = null;
        this.arquivoPagamentos = null;
        this.repositorioAssinantes = null;
        this.repositorioPlanos = null;
        this.cardapio = null;
    }

    /** Repositório persistente: carrega as assinaturas já gravadas na pasta informada. */
    public RepositorioAssinaturas(Path diretorioDados, RepositorioAssinantes repositorioAssinantes,
                                  RepositorioPlanos repositorioPlanos, Cardapio cardapio) {
        this.arquivoAssinaturas = new ArquivoTexto(diretorioDados, ARQUIVO_ASSINATURAS);
        this.arquivoItens = new ArquivoTexto(diretorioDados, ARQUIVO_ITENS);
        this.arquivoPagamentos = new ArquivoTexto(diretorioDados, ARQUIVO_PAGAMENTOS);
        this.repositorioAssinantes = repositorioAssinantes;
        this.repositorioPlanos = repositorioPlanos;
        this.cardapio = cardapio;
        carregar();
    }

    public void salvar(Assinatura assinatura) {
        porCodigo.put(assinatura.getCodigo(), assinatura);
        gravar();
    }

    public List<Assinatura> listarTodas() {
        return Collections.unmodifiableList(new ArrayList<>(porCodigo.values()));
    }

    public Optional<Assinatura> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(porCodigo.get(codigo));
    }

    public List<Assinatura> listarPorCelular(String celular) {
        List<Assinatura> encontradas = new ArrayList<>();
        for (Assinatura a : porCodigo.values()) {
            if (a.getAssinante().getCelular().equals(celular)) {
                encontradas.add(a);
            }
        }
        return encontradas;
    }

    public boolean isPersistente() {
        return arquivoAssinaturas != null;
    }

    public Path getArquivo() {
        return arquivoAssinaturas == null ? null : arquivoAssinaturas.getArquivo();
    }

    // ---------- gravação ----------

    private void gravar() {
        if (arquivoAssinaturas == null) {
            return;
        }
        List<String[]> assinaturas = new ArrayList<>();
        List<String[]> itens = new ArrayList<>();
        List<String[]> pagamentos = new ArrayList<>();

        for (Assinatura a : porCodigo.values()) {
            assinaturas.add(new String[] {
                    a.getCodigo(),
                    a.getAssinante().getCelular(),
                    String.valueOf(a.getPlano().getId()),
                    a.getDataCriacao().toString(),
                    a.getStatus().name(),
                    a.getPedido().getStatus().name(),
                    a.getProtocolo() == null ? "" : a.getProtocolo(),
                    a.getPrevisaoPrimeiraEntrega() == null ? "" : a.getPrevisaoPrimeiraEntrega().toString()
            });
            for (ItemPedido ip : a.getPedido().getItens()) {
                itens.add(new String[] {
                        a.getCodigo(),
                        ip.getItem().getCodigo(),
                        String.valueOf(ip.getQuantidade())
                });
            }
            for (Pagamento p : a.getPagamentos()) {
                pagamentos.add(new String[] {
                        a.getCodigo(),
                        p.getDataHora().toString(),
                        p.getValor().toPlainString(),
                        p.getCartaoMascarado(),
                        p.getStatus().name(),
                        p.getCodigoAutorizacao() == null ? "" : p.getCodigoAutorizacao()
                });
            }
        }
        arquivoAssinaturas.gravarRegistros(assinaturas);
        arquivoItens.gravarRegistros(itens);
        arquivoPagamentos.gravarRegistros(pagamentos);
    }

    // ---------- carga (registro -> objeto de domínio) ----------

    private void carregar() {
        Map<String, List<String[]>> itensPorAssinatura = agruparPorCodigo(arquivoItens.lerRegistros());
        Map<String, List<String[]>> pagamentosPorAssinatura = agruparPorCodigo(arquivoPagamentos.lerRegistros());

        for (String[] campos : arquivoAssinaturas.lerRegistros()) {
            if (campos.length < 8) {
                continue;
            }
            String codigo = campos[0];
            Optional<Assinante> assinante = repositorioAssinantes.buscarPorCelular(campos[1]);
            Optional<PlanoAssinatura> plano = repositorioPlanos.buscarPorId(Integer.parseInt(campos[2]));
            if (assinante.isEmpty() || plano.isEmpty()) {
                continue; // registro órfão: ignorado em vez de interromper a aplicação
            }

            Pedido pedido = Pedido.restaurar(plano.get(), StatusPedido.valueOf(campos[5]));
            for (String[] item : itensPorAssinatura.getOrDefault(codigo, List.of())) {
                Optional<ItemCardapio> itemCardapio = cardapio.buscarPorCodigo(item[1]);
                itemCardapio.ifPresent(ic -> pedido.restaurarItem(ic, Integer.parseInt(item[2])));
            }

            Assinatura assinatura = Assinatura.restaurar(assinante.get(), plano.get(), pedido, codigo,
                    LocalDateTime.parse(campos[3]), StatusAssinatura.valueOf(campos[4]),
                    campos[6].isBlank() ? null : campos[6],
                    campos[7].isBlank() ? null : LocalDate.parse(campos[7]));

            for (String[] pagamento : pagamentosPorAssinatura.getOrDefault(codigo, List.of())) {
                assinatura.restaurarPagamento(Pagamento.restaurar(
                        new BigDecimal(pagamento[2]),
                        LocalDateTime.parse(pagamento[1]),
                        pagamento[3],
                        StatusPagamento.valueOf(pagamento[4]),
                        pagamento[5].isBlank() ? null : pagamento[5]));
            }
            porCodigo.put(codigo, assinatura);
        }
    }

    private static Map<String, List<String[]>> agruparPorCodigo(List<String[]> registros) {
        Map<String, List<String[]>> agrupados = new LinkedHashMap<>();
        for (String[] campos : registros) {
            agrupados.computeIfAbsent(campos[0], c -> new ArrayList<>()).add(campos);
        }
        return agrupados;
    }
}
