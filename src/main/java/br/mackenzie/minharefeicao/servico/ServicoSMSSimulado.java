package br.mackenzie.minharefeicao.servico;

/** Implementação simulada: "envia" o SMS imprimindo no console. */
public class ServicoSMSSimulado implements ServicoSMS {
    private String ultimaMensagem;
    private final boolean exibirNoConsole;

    public ServicoSMSSimulado(boolean exibirNoConsole) {
        this.exibirNoConsole = exibirNoConsole;
    }

    @Override
    public void enviar(String celular, String mensagem) {
        this.ultimaMensagem = mensagem;
        if (exibirNoConsole) {
            System.out.println("  [SMS simulado -> " + celular + "] " + mensagem);
        }
    }

    /** Apoio à demonstração automática: lê o último código "recebido". */
    public String getUltimoCodigoEnviado() {
        return ultimaMensagem == null ? null : ultimaMensagem.replaceAll("\\D", "");
    }
}
