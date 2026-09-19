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
import br.mackenzie.minharefeicao.ui.TelaAssinatura;

import java.util.Random;
import java.util.Scanner;

/**
 * Ponto de entrada. Monta as dependências (injeção via construtor) e inicia:
 *  - modo interativo:  java ... App
 *  - demonstração:     java ... App --demo
 */
public class App {
    public static void main(String[] args) {
        RepositorioPlanos planos = DadosIniciais.criarPlanos();
        Cardapio cardapio = DadosIniciais.criarCardapio();
        RepositorioAssinantes assinantes = new RepositorioAssinantes();
        RepositorioAssinaturas assinaturas = new RepositorioAssinaturas();
        GeradorProtocolo protocolo = new GeradorProtocolo();
        OperadoraCartaoSimulada operadora = new OperadoraCartaoSimulada();
        Random random = new Random();

        if (args.length > 0 && args[0].equals("--demo")) {
            new DemonstracaoCenarios(sms -> new AssinarPlanoController(sms, operadora, planos,
                    assinantes, assinaturas, cardapio, protocolo, random)).executarTodos();
            return;
        }

        AssinarPlanoController controller = new AssinarPlanoController(new ServicoSMSSimulado(true), operadora,
                planos, assinantes, assinaturas, cardapio, protocolo, random);
        new TelaAssinatura(controller, new Scanner(System.in)).executar();
    }
}
