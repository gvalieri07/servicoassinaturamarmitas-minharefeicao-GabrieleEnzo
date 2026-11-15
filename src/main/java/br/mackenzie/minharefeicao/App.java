package br.mackenzie.minharefeicao;

import br.mackenzie.minharefeicao.controle.AssinarPlanoController;
import br.mackenzie.minharefeicao.dominio.Cardapio;
import br.mackenzie.minharefeicao.persistencia.DadosIniciais;
import br.mackenzie.minharefeicao.persistencia.RepositorioAssinantes;
import br.mackenzie.minharefeicao.persistencia.RepositorioAssinaturas;
import br.mackenzie.minharefeicao.persistencia.RepositorioPlanos;
import br.mackenzie.minharefeicao.servico.GeradorProtocolo;
import br.mackenzie.minharefeicao.servico.OperadoraCartaoSimulada;
import br.mackenzie.minharefeicao.servico.ServicoSMSSimulado;
import br.mackenzie.minharefeicao.ui.DemonstracaoCenarios;
import br.mackenzie.minharefeicao.ui.MenuPrincipal;

import java.nio.file.Path;
import java.util.Random;
import java.util.Scanner;

/**
 * Ponto de entrada da aplicação. Monta as dependências (injeção por construtor)
 * e inicia a camada de apresentação.
 *
 * <pre>
 *   java -jar minha-refeicao.jar                  modo interativo (menu)
 *   java -jar minha-refeicao.jar --demo           demonstração automática dos fluxos
 *   java -jar minha-refeicao.jar --dados ./dados  pasta dos arquivos de persistência
 * </pre>
 */
public class App {

    private static final String PASTA_PADRAO = "dados";

    public static void main(String[] args) {
        // Catálogos pré-cadastrados (carregados em memória na inicialização)
        RepositorioPlanos planos = DadosIniciais.criarPlanos();
        Cardapio cardapio = DadosIniciais.criarCardapio();

        GeradorProtocolo geradorProtocolo = new GeradorProtocolo();
        OperadoraCartaoSimulada operadora = new OperadoraCartaoSimulada();
        Random random = new Random();

        // Demonstração automática: repositórios em memória, não grava arquivos
        if (temOpcao(args, "--demo")) {
            executarDemonstracao(planos, cardapio, operadora, geradorProtocolo, random);
            return;
        }

        // Persistência em arquivos de texto
        Path pastaDados = Path.of(valorDaOpcao(args, "--dados", PASTA_PADRAO));
        RepositorioAssinantes assinantes = new RepositorioAssinantes(pastaDados);
        RepositorioAssinaturas assinaturas = new RepositorioAssinaturas(pastaDados, assinantes, planos, cardapio);

        Scanner teclado = new Scanner(System.in);
        MenuPrincipal menu = new MenuPrincipal(
                teclado,
                () -> new AssinarPlanoController(new ServicoSMSSimulado(true), operadora, planos,
                        assinantes, assinaturas, cardapio, geradorProtocolo, random),
                assinaturas,
                () -> executarDemonstracao(planos, cardapio, operadora, geradorProtocolo, random));
        menu.executar();
    }

    private static void executarDemonstracao(RepositorioPlanos planos, Cardapio cardapio,
                                             OperadoraCartaoSimulada operadora,
                                             GeradorProtocolo geradorProtocolo, Random random) {
        new DemonstracaoCenarios(sms -> new AssinarPlanoController(sms, operadora, planos,
                new RepositorioAssinantes(), new RepositorioAssinaturas(), cardapio, geradorProtocolo, random))
                .executarTodos();
    }

    private static boolean temOpcao(String[] args, String opcao) {
        for (String arg : args) {
            if (arg.equals(opcao)) {
                return true;
            }
        }
        return false;
    }

    private static String valorDaOpcao(String[] args, String opcao, String padrao) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(opcao)) {
                return args[i + 1];
            }
        }
        return padrao;
    }
}
