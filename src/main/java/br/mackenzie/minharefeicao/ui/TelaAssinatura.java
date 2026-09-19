package br.mackenzie.minharefeicao.ui;

import br.mackenzie.minharefeicao.controle.AssinarPlanoController;
import br.mackenzie.minharefeicao.controle.ConfirmacaoAssinatura;
import br.mackenzie.minharefeicao.dominio.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Camada de apresentação (console). Só conversa com o controlador; as regras
 * de negócio ficam no domínio. Cada método corresponde a uma tela do storyboard.
 */
public class TelaAssinatura {
    private final AssinarPlanoController controller;
    private final Scanner in;

    public TelaAssinatura(AssinarPlanoController controller, Scanner in) {
        this.controller = controller;
        this.in = in;
    }

    public void executar() {
        titulo("MINHA REFEIÇÃO — Assinar Plano de Refeições");
        telaCelular();
        telaCodigo();
        telaPlanos();
        telaPreferencias();
        telaSelecao(CategoriaItem.PRATO_PRINCIPAL, true);
        telaSelecao(CategoriaItem.ACOMPANHAMENTO, false);
        telaSelecao(CategoriaItem.SOBREMESA, false);
        telaResumoPedido();
        telaEndereco();
        telaPagamento();
    }

    // Passos 1-2
    private void telaCelular() {
        titulo("1. Identificação");
        while (true) {
            try {
                controller.informarCelular(ler("Informe seu celular (DDD + número): "));
                System.out.println("Enviamos um código de confirmação por SMS.");
                return;
            } catch (RegraNegocioException e) {
                erro(e.getMessage());
            }
        }
    }

    // Passos 3-4 + FA1
    private void telaCodigo() {
        titulo("2. Código de confirmação");
        while (true) {
            try {
                if (controller.validarCodigo(ler("Informe o código recebido: "))) {
                    System.out.println("Código validado com sucesso!");
                    return;
                }
                erro("Código inválido. Tentativas restantes: " + controller.getTentativasRestantes());
            } catch (TentativasEsgotadasException e) {
                erro(e.getMessage());
                ler("Pressione ENTER para receber um novo código...");
                controller.solicitarNovoCodigo();
            }
        }
    }

    // Passos 5-6
    private void telaPlanos() {
        titulo("3. Planos de assinatura");
        List<PlanoAssinatura> planos = controller.listarPlanos();
        for (PlanoAssinatura p : planos) {
            System.out.printf("  [%d] %-11s | %2d refeições | %-9s | %s%n", p.getId(), p.getNome(),
                    p.getQuantidadeRefeicoes(), p.getPeriodicidade().getDescricao(), Formatador.moeda(p.getValor()));
        }
        while (true) {
            try {
                controller.selecionarPlano(lerInteiro("Escolha o plano: "));
                if (confirmar("Confirmar plano selecionado?")) return;
            } catch (RegraNegocioException e) {
                erro(e.getMessage());
            }
        }
    }

    // Passos 7-9
    private void telaPreferencias() {
        titulo("4. Preferências alimentares");
        PreferenciaAlimentar[] opcoes = PreferenciaAlimentar.values();
        for (int i = 0; i < opcoes.length; i++) {
            System.out.printf("  [%d] %s%n", i + 1, opcoes[i].getDescricao());
        }
        while (true) {
            try {
                String entrada = ler("Informe uma ou mais opções separadas por vírgula (ex.: 2,3): ");
                Set<PreferenciaAlimentar> escolhidas = EnumSet.noneOf(PreferenciaAlimentar.class);
                for (String parte : entrada.split(",")) {
                    int idx = Integer.parseInt(parte.trim()) - 1;
                    if (idx < 0 || idx >= opcoes.length) throw new NumberFormatException();
                    escolhidas.add(opcoes[idx]);
                }
                controller.registrarPreferencias(escolhidas);
                System.out.println("Preferências registradas.");
                return;
            } catch (NumberFormatException e) {
                erro("Opção inválida.");
            } catch (RegraNegocioException e) {
                erro(e.getMessage());
            }
        }
    }

    // Passos 10-18 + FA2
    private void telaSelecao(CategoriaItem categoria, boolean obrigatorio) {
        titulo("Seleção — " + categoria.getDescricao());
        List<ItemCardapio> itens = controller.listarItens(categoria);
        if (itens.isEmpty()) {
            System.out.println("Nenhum item disponível para suas preferências.");
            return;
        }
        for (ItemCardapio item : itens) {
            System.out.printf("  [%s] %s%n", item.getCodigo(), item.getNome());
        }
        while (true) {
            int disponivel = controller.getQuantidadeDisponivel(categoria);
            System.out.println("Você ainda pode escolher " + disponivel + " unidade(s) desta categoria.");
            String cod = ler("Código do item (ENTER para concluir): ");
            if (cod.isBlank()) {
                if (obrigatorio && controller.obterPedido().getQuantidadeSelecionada(categoria) == 0) {
                    erro("Selecione ao menos um prato principal.");
                    continue;
                }
                return;
            }
            try {
                int qtd = lerInteiro("Quantidade: ");
                controller.adicionarAoPedido(cod, qtd);
                System.out.println("Adicionado ao pedido.");
            } catch (LimiteRefeicoesExcedidoException e) {
                erro(e.getMessage()); // FA2.2 e FA2.3
            } catch (RegraNegocioException e) {
                erro(e.getMessage());
            }
        }
    }

    // Passo 19
    private void telaResumoPedido() {
        titulo("Resumo do pedido");
        Pedido pedido = controller.obterPedido();
        for (CategoriaItem c : CategoriaItem.values()) {
            System.out.println(c.getDescricao() + ":");
            for (ItemPedido ip : pedido.getItens(c)) {
                System.out.println("   - " + ip);
            }
        }
    }

    // Passos 20-22
    private void telaEndereco() {
        titulo("5. Endereço de entrega");
        while (true) {
            try {
                Endereco e = new Endereco(ler("Logradouro: "), ler("Número: "), ler("Complemento: "),
                        ler("Bairro: "), ler("Cidade: "), ler("UF: "), ler("CEP: "));
                System.out.println("Entregar em: " + e);
                if (!confirmar("Confirma o endereço?")) continue;
                controller.informarEndereco(e);
                System.out.println("Endereço armazenado. Status da assinatura: " + controller.getStatusAssinatura().getDescricao());
                return;
            } catch (RegraNegocioException ex) {
                erro(ex.getMessage());
            }
        }
    }

    // Passos 24-31 + FA3
    private void telaPagamento() {
        titulo("6. Pagamento");
        System.out.println("Valor total da assinatura: " + Formatador.moeda(controller.obterValorTotal()));
        while (true) {
            try {
                ConfirmacaoAssinatura c = controller.pagar(ler("Número do cartão: "), ler("Nome do titular: "),
                        ler("Validade (MM/AA): "), ler("CVV: "));
                exibirConfirmacao(c);
                return;
            } catch (RegraNegocioException e) {
                erro(e.getMessage());
                if (!confirmar("Deseja informar outro cartão ou tentar novamente?")) {
                    controller.desistirDaAssinatura();
                    System.out.println("Processo encerrado sem ativação do plano. Status: "
                            + controller.getStatusAssinatura().getDescricao());
                    return;
                }
            }
        }
    }

    static void exibirConfirmacao(ConfirmacaoAssinatura c) {
        titulo("ASSINATURA CONFIRMADA!");
        System.out.println("Protocolo.............: " + c.getProtocolo());
        System.out.println("Plano contratado......: " + c.getPlano());
        System.out.println("Periodicidade.........: " + c.getPeriodicidade());
        System.out.println("Refeições selecionadas:");
        c.getRefeicoes().forEach(r -> System.out.println("   - " + r));
        System.out.println("Endereço de entrega...: " + c.getEnderecoEntrega());
        System.out.println("Primeira entrega......: " + Formatador.data(c.getPrevisaoPrimeiraEntrega()));
        System.out.println("Valor.................: " + Formatador.moeda(c.getValorTotal()));
        System.out.println("Status assinatura/pedido: " + c.getStatusAssinatura() + " / " + c.getStatusPedido());
    }

    // ---------- utilitários de entrada ----------

    private String ler(String rotulo) {
        System.out.print(rotulo);
        return in.hasNextLine() ? in.nextLine().trim() : "";
    }

    private int lerInteiro(String rotulo) {
        while (true) {
            try {
                return Integer.parseInt(ler(rotulo));
            } catch (NumberFormatException e) {
                erro("Digite um número inteiro.");
            }
        }
    }

    private boolean confirmar(String pergunta) {
        return ler(pergunta + " (s/n): ").toLowerCase().startsWith("s");
    }

    static void titulo(String t) {
        System.out.println();
        System.out.println("=== " + t + " ===");
    }

    private static void erro(String msg) { System.out.println("  (!) " + msg); }
}
