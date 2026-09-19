package br.mackenzie.minharefeicao.controle;

import br.mackenzie.minharefeicao.dominio.*;
import br.mackenzie.minharefeicao.persistencia.RepositorioAssinantes;
import br.mackenzie.minharefeicao.persistencia.RepositorioAssinaturas;
import br.mackenzie.minharefeicao.persistencia.RepositorioPlanos;
import br.mackenzie.minharefeicao.servico.GeradorProtocolo;
import br.mackenzie.minharefeicao.servico.OperadoraCartao;
import br.mackenzie.minharefeicao.servico.RespostaAutorizacao;
import br.mackenzie.minharefeicao.servico.ServicoSMS;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Controlador do caso de uso "Assinar Plano de Refeições" (GRASP Controller).
 * Recebe as operações de sistema vindas da interface e as delega aos objetos
 * de domínio e serviços. Não contém regras de negócio: elas ficam nas classes
 * especialistas (CodigoConfirmacao, Pedido, Assinatura, ItemCardapio...).
 */
public class AssinarPlanoController {
    private final ServicoSMS servicoSMS;
    private final OperadoraCartao operadora;
    private final RepositorioPlanos repositorioPlanos;
    private final RepositorioAssinantes repositorioAssinantes;
    private final RepositorioAssinaturas repositorioAssinaturas;
    private final Cardapio cardapio;
    private final GeradorProtocolo geradorProtocolo;
    private final Random random;

    // Estado da sessão do caso de uso
    private Assinante assinante;
    private CodigoConfirmacao codigo;
    private Assinatura assinatura;

    public AssinarPlanoController(ServicoSMS servicoSMS, OperadoraCartao operadora,
                                  RepositorioPlanos repositorioPlanos,
                                  RepositorioAssinantes repositorioAssinantes,
                                  RepositorioAssinaturas repositorioAssinaturas,
                                  Cardapio cardapio, GeradorProtocolo geradorProtocolo,
                                  Random random) {
        this.servicoSMS = servicoSMS;
        this.operadora = operadora;
        this.repositorioPlanos = repositorioPlanos;
        this.repositorioAssinantes = repositorioAssinantes;
        this.repositorioAssinaturas = repositorioAssinaturas;
        this.cardapio = cardapio;
        this.geradorProtocolo = geradorProtocolo;
        this.random = random;
    }

    // ---------- Passos 1-4 (+ FA1) ----------

    public void informarCelular(String celular) {
        String normalizado = Assinante.normalizarCelular(celular);
        assinante = repositorioAssinantes.buscarPorCelular(normalizado)
                .orElseGet(() -> new Assinante(normalizado));
        enviarNovoCodigo();
    }

    /** FA1.5 – após três tentativas inválidas, o assinante solicita novo código. */
    public void solicitarNovoCodigo() {
        exigirAssinante();
        enviarNovoCodigo();
    }

    private void enviarNovoCodigo() {
        codigo = CodigoConfirmacao.gerar(random);
        servicoSMS.enviar(assinante.getCelular(), codigo.gerarMensagemSMS());
    }

    /**
     * @return true se o código for válido; false se inválido (FA1.2/FA1.3).
     * @throws TentativasEsgotadasException na terceira tentativa inválida (FA1.5).
     */
    public boolean validarCodigo(String codigoInformado) {
        if (codigo == null) {
            throw new RegraNegocioException("Nenhum código foi enviado. Informe o celular.");
        }
        boolean valido = codigo.validar(codigoInformado);
        if (valido) {
            assinante.confirmarCelular();
            repositorioAssinantes.salvar(assinante);
        }
        return valido;
    }

    public int getTentativasRestantes() { return codigo == null ? 0 : codigo.getTentativasRestantes(); }

    // ---------- Passos 5-6 ----------

    public List<PlanoAssinatura> listarPlanos() {
        exigirCelularValidado();
        return repositorioPlanos.listarDisponiveis();
    }

    public void selecionarPlano(int idPlano) {
        exigirCelularValidado();
        PlanoAssinatura plano = repositorioPlanos.buscarPorId(idPlano)
                .orElseThrow(() -> new RegraNegocioException("Plano inexistente: " + idPlano));
        assinatura = new Assinatura(assinante, plano);
    }

    // ---------- Passos 7-9 ----------

    public void registrarPreferencias(Set<PreferenciaAlimentar> preferencias) {
        exigirAssinatura();
        assinante.registrarPreferencias(preferencias);
        repositorioAssinantes.salvar(assinante);
    }

    // ---------- Passos 10-18 (+ FA2) ----------

    public List<ItemCardapio> listarItens(CategoriaItem categoria) {
        exigirAssinatura();
        return cardapio.listarDisponiveis(categoria, assinante.getPreferencias());
    }

    /** @throws LimiteRefeicoesExcedidoException quando excede o plano (FA2). */
    public void adicionarAoPedido(String codigoItem, int quantidade) {
        exigirAssinatura();
        ItemCardapio item = cardapio.buscarPorCodigo(codigoItem)
                .filter(ItemCardapio::isDisponivel)
                .filter(i -> i.atende(assinante.getPreferencias()))
                .orElseThrow(() -> new RegraNegocioException("Item não disponível para suas preferências: " + codigoItem));
        assinatura.adicionarItemAoPedido(item, quantidade);
    }

    public int getQuantidadeDisponivel(CategoriaItem categoria) {
        exigirAssinatura();
        return assinatura.getPedido().getQuantidadeDisponivel(categoria);
    }

    /** Passo 19 – composição completa do pedido (leitura). */
    public Pedido obterPedido() {
        exigirAssinatura();
        return assinatura.getPedido();
    }

    // ---------- Passos 20-24 ----------

    public void informarEndereco(Endereco endereco) {
        exigirAssinatura();
        assinante.definirEnderecoEntrega(endereco);
        repositorioAssinantes.salvar(assinante);
        assinatura.aguardarPagamento();
        repositorioAssinaturas.salvar(assinatura);
    }

    public BigDecimal obterValorTotal() {
        exigirAssinatura();
        return assinatura.calcularValorTotal();
    }

    // ---------- Passos 25-31 (+ FA3) ----------

    /**
     * @return dados da confirmação (passo 31) quando aprovado.
     * @throws PagamentoNaoAutorizadoException quando a operadora recusa (FA3).
     */
    public ConfirmacaoAssinatura pagar(String numeroCartao, String titular, String validadeMMAA, String cvv) {
        exigirAssinatura();
        CartaoCredito cartao = new CartaoCredito(numeroCartao, titular, validadeMMAA, cvv);
        BigDecimal valor = assinatura.calcularValorTotal();
        RespostaAutorizacao resposta = operadora.autorizar(cartao, valor);
        Pagamento pagamento = new Pagamento(valor, cartao, resposta.isAprovado(), resposta.getCodigoAutorizacao());
        assinatura.registrarPagamento(pagamento);
        repositorioAssinaturas.salvar(assinatura);
        if (!resposta.isAprovado()) {
            throw new PagamentoNaoAutorizadoException(resposta.getMensagem());
        }
        assinatura.atribuirProtocolo(geradorProtocolo.gerar());
        return ConfirmacaoAssinatura.de(assinatura);
    }

    /** FA3.6 – o assinante não deseja tentar novamente. */
    public void desistirDaAssinatura() {
        exigirAssinatura();
        assinatura.encerrarSemAtivacao();
        repositorioAssinaturas.salvar(assinatura);
    }

    public StatusAssinatura getStatusAssinatura() {
        return assinatura == null ? null : assinatura.getStatus();
    }

    // ---------- Pré-condições de navegação ----------

    private void exigirAssinante() {
        if (assinante == null) throw new RegraNegocioException("Informe o número de celular primeiro.");
    }

    private void exigirCelularValidado() {
        exigirAssinante();
        if (!assinante.isCelularVerificado()) throw new RegraNegocioException("Valide o código de confirmação primeiro.");
    }

    private void exigirAssinatura() {
        if (assinatura == null) throw new RegraNegocioException("Selecione um plano primeiro.");
    }
}
