package br.mackenzie.minharefeicao.ui;

import br.mackenzie.minharefeicao.dominio.Assinatura;
import br.mackenzie.minharefeicao.dominio.CategoriaItem;
import br.mackenzie.minharefeicao.dominio.ItemPedido;
import br.mackenzie.minharefeicao.dominio.Pagamento;
import br.mackenzie.minharefeicao.persistencia.RepositorioAssinaturas;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Objeto fronteira de consulta: mostra as assinaturas recuperadas dos arquivos
 * de dados, com o pedido e as tentativas de pagamento de cada uma.
 */
public class TelaConsultaAssinaturas {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RepositorioAssinaturas repositorio;

    public TelaConsultaAssinaturas(RepositorioAssinaturas repositorio) {
        this.repositorio = repositorio;
    }

    public void exibir() {
        TelaAssinatura.titulo("Assinaturas gravadas");
        List<Assinatura> assinaturas = repositorio.listarTodas();
        if (assinaturas.isEmpty()) {
            System.out.println("Nenhuma assinatura registrada até o momento.");
            return;
        }
        for (Assinatura a : assinaturas) {
            System.out.println();
            System.out.println("Código................: " + a.getCodigo());
            System.out.println("Contratada em.........: " + a.getDataCriacao().format(DATA_HORA));
            System.out.println("Assinante.............: " + a.getAssinante().getCelularMascarado());
            System.out.println("Plano.................: " + a.getPlano().getNome()
                    + " (" + a.getPlano().getQuantidadeRefeicoes() + " refeições, "
                    + a.getPlano().getPeriodicidade().getDescricao() + ")");
            System.out.println("Valor.................: " + Formatador.moeda(a.calcularValorTotal()));
            System.out.println("Status assinatura.....: " + a.getStatus().getDescricao());
            System.out.println("Status do pedido......: " + a.getPedido().getStatus().getDescricao());
            if (a.getProtocolo() != null) {
                System.out.println("Protocolo.............: " + a.getProtocolo());
            }
            if (a.getPrevisaoPrimeiraEntrega() != null) {
                System.out.println("Primeira entrega......: " + Formatador.data(a.getPrevisaoPrimeiraEntrega()));
            }
            if (a.getAssinante().getEnderecoEntrega() != null) {
                System.out.println("Endereço de entrega...: " + a.getAssinante().getEnderecoEntrega());
            }
            System.out.println("Composição do pedido..:");
            for (CategoriaItem categoria : CategoriaItem.values()) {
                for (ItemPedido ip : a.getPedido().getItens(categoria)) {
                    System.out.println("   - " + categoria.getDescricao() + ": " + ip);
                }
            }
            for (Pagamento p : a.getPagamentos()) {
                System.out.printf("Pagamento.............: %s | %s | %s | %s%n",
                        p.getDataHora().format(DATA_HORA), Formatador.moeda(p.getValor()),
                        p.getCartaoMascarado(), p.getStatus());
            }
        }
        System.out.println();
        System.out.println("Origem dos dados: " + repositorio.getArquivo().toAbsolutePath());
    }
}
