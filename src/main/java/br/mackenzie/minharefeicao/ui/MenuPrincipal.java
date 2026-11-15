package br.mackenzie.minharefeicao.ui;

import br.mackenzie.minharefeicao.controle.AssinarPlanoController;
import br.mackenzie.minharefeicao.dominio.RegraNegocioException;
import br.mackenzie.minharefeicao.persistencia.RepositorioAssinaturas;

import java.util.Scanner;
import java.util.function.Supplier;

/**
 * Objeto fronteira de entrada da aplicação. Mantém o sistema em execução entre
 * as assinaturas e dá acesso ao que já foi gravado nos arquivos de dados —
 * é por aqui que se enxerga a persistência entre duas execuções diferentes.
 */
public class MenuPrincipal {

    private final Scanner in;
    private final Supplier<AssinarPlanoController> novaSessao;
    private final RepositorioAssinaturas repositorioAssinaturas;
    private final Runnable demonstracao;

    public MenuPrincipal(Scanner in, Supplier<AssinarPlanoController> novaSessao,
                         RepositorioAssinaturas repositorioAssinaturas, Runnable demonstracao) {
        this.in = in;
        this.novaSessao = novaSessao;
        this.repositorioAssinaturas = repositorioAssinaturas;
        this.demonstracao = demonstracao;
    }

    public void executar() {
        System.out.println();
        System.out.println("==========================================================");
        System.out.println("   MINHA REFEIÇÃO — Serviço de Assinatura de Marmitas");
        System.out.println("   Caso de uso: Assinar Plano de Refeições");
        System.out.println("==========================================================");
        if (repositorioAssinaturas.isPersistente()) {
            System.out.println("Dados gravados em: " + repositorioAssinaturas.getArquivo().getParent().toAbsolutePath());
            System.out.println("Assinaturas já registradas: " + repositorioAssinaturas.listarTodas().size());
        }

        while (true) {
            System.out.println();
            System.out.println("--- Menu principal ---");
            System.out.println("  [1] Assinar plano de refeições");
            System.out.println("  [2] Consultar assinaturas gravadas");
            System.out.println("  [3] Demonstração automática (fluxo principal + FA1, FA2 e FA3)");
            System.out.println("  [0] Sair");
            System.out.print("Escolha uma opção: ");
            String opcao = in.hasNextLine() ? in.nextLine().trim() : "0";

            switch (opcao) {
                case "1" -> assinar();
                case "2" -> new TelaConsultaAssinaturas(repositorioAssinaturas).exibir();
                case "3" -> demonstracao.run();
                case "0" -> {
                    System.out.println("Até logo!");
                    return;
                }
                default -> System.out.println("  (!) Opção inválida.");
            }
        }
    }

    /** Cada assinatura é uma nova sessão do caso de uso, com o seu próprio controlador. */
    private void assinar() {
        try {
            new TelaAssinatura(novaSessao.get(), in).executar();
        } catch (RegraNegocioException e) {
            System.out.println("  (!) Processo interrompido: " + e.getMessage());
        }
    }
}
