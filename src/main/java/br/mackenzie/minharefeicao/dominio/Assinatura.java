package br.mackenzie.minharefeicao.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Assinatura de um plano. Controla o ciclo de vida (status) e compõe o
 * Pedido e os Pagamentos.
 *
 * <p>O atributo {@code codigo} identifica a assinatura de forma única e é o
 * que permite relacioná-la aos itens do pedido e aos pagamentos gravados nos
 * arquivos de dados.</p>
 */
public class Assinatura {
    public static final int DIAS_PARA_PRIMEIRA_ENTREGA = 3;

    private static final AtomicInteger SEQUENCIA = new AtomicInteger(0);
    private static final DateTimeFormatter FORMATO_CODIGO = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final String codigo;
    private final LocalDateTime dataCriacao;
    private final Assinante assinante;
    private final PlanoAssinatura plano;
    private final Pedido pedido;
    private final List<Pagamento> pagamentos = new ArrayList<>();
    private StatusAssinatura status = StatusAssinatura.EM_ELABORACAO;
    private String protocolo;
    private LocalDate previsaoPrimeiraEntrega;

    /** Passo 7: a assinatura nasce "Em elaboração" e já cria o seu Pedido (Creator). */
    public Assinatura(Assinante assinante, PlanoAssinatura plano) {
        this(exigirCelularVerificado(assinante), plano, gerarCodigo(), LocalDateTime.now(), new Pedido(plano));
    }

    private Assinatura(Assinante assinante, PlanoAssinatura plano, String codigo,
                       LocalDateTime dataCriacao, Pedido pedido) {
        this.assinante = assinante;
        this.plano = plano;
        this.codigo = codigo;
        this.dataCriacao = dataCriacao;
        this.pedido = pedido;
    }

    private static Assinante exigirCelularVerificado(Assinante assinante) {
        if (!assinante.isCelularVerificado()) {
            throw new RegraNegocioException("Celular do assinante ainda não foi validado.");
        }
        return assinante;
    }

    private static String gerarCodigo() {
        return String.format("AS-%s-%03d", LocalDateTime.now().format(FORMATO_CODIGO),
                SEQUENCIA.incrementAndGet() % 1000);
    }

    public BigDecimal calcularValorTotal() { return plano.getValor(); }

    /** Passos 12, 15 e 18: delega ao Pedido (evita que o controlador navegue pelo grafo). */
    public void adicionarItemAoPedido(ItemCardapio item, int quantidade) {
        exigirStatus(StatusAssinatura.EM_ELABORACAO);
        pedido.adicionarItem(item, quantidade);
    }

    /** Passo 23: pedido completo + endereço definido => Aguardando Pagamento. */
    public void aguardarPagamento() {
        exigirStatus(StatusAssinatura.EM_ELABORACAO);
        if (!pedido.possuiPratoPrincipal()) {
            throw new RegraNegocioException("Selecione ao menos um prato principal.");
        }
        if (assinante.getEnderecoEntrega() == null) {
            throw new RegraNegocioException("Informe o endereço de entrega.");
        }
        status = StatusAssinatura.AGUARDANDO_PAGAMENTO;
    }

    /**
     * Passos 28-29 / FA3: registra o pagamento; se aprovado ativa a assinatura
     * e aprova o pedido, senão permanece Aguardando Pagamento.
     */
    public void registrarPagamento(Pagamento pagamento) {
        exigirStatus(StatusAssinatura.AGUARDANDO_PAGAMENTO);
        pagamentos.add(pagamento);
        if (pagamento.isAprovado()) {
            status = StatusAssinatura.ATIVA;
            pedido.aprovar();
            previsaoPrimeiraEntrega = LocalDate.now().plusDays(DIAS_PARA_PRIMEIRA_ENTREGA);
        }
    }

    public void atribuirProtocolo(String protocolo) {
        exigirStatus(StatusAssinatura.ATIVA);
        this.protocolo = protocolo;
    }

    /** FA3.6: o assinante desiste; a assinatura é encerrada sem ativação. */
    public void encerrarSemAtivacao() {
        exigirStatus(StatusAssinatura.AGUARDANDO_PAGAMENTO);
        status = StatusAssinatura.ENCERRADA_SEM_ATIVACAO;
        pedido.cancelar();
    }

    private void exigirStatus(StatusAssinatura esperado) {
        if (status != esperado) {
            throw new RegraNegocioException("Operação inválida: assinatura está " + status.getDescricao() + ".");
        }
    }

    // ---------- reconstituição a partir dos arquivos de dados ----------

    /**
     * Recria uma assinatura já gravada, sem passar pela máquina de estados:
     * os estados registrados no arquivo são o resultado de transições que já
     * aconteceram em uma execução anterior.
     */
    public static Assinatura restaurar(Assinante assinante, PlanoAssinatura plano, Pedido pedido,
                                       String codigo, LocalDateTime dataCriacao, StatusAssinatura status,
                                       String protocolo, LocalDate previsaoPrimeiraEntrega) {
        Assinatura assinatura = new Assinatura(assinante, plano, codigo, dataCriacao, pedido);
        assinatura.status = status;
        assinatura.protocolo = protocolo;
        assinatura.previsaoPrimeiraEntrega = previsaoPrimeiraEntrega;
        return assinatura;
    }

    /** Acrescenta um pagamento lido do arquivo, sem reaplicar as regras de ativação. */
    public void restaurarPagamento(Pagamento pagamento) {
        pagamentos.add(pagamento);
    }

    public String getCodigo() { return codigo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public Assinante getAssinante() { return assinante; }
    public PlanoAssinatura getPlano() { return plano; }
    public Pedido getPedido() { return pedido; }
    public StatusAssinatura getStatus() { return status; }
    public String getProtocolo() { return protocolo; }
    public LocalDate getPrevisaoPrimeiraEntrega() { return previsaoPrimeiraEntrega; }
    public List<Pagamento> getPagamentos() { return Collections.unmodifiableList(pagamentos); }
}
