package br.mackenzie.minharefeicao.servico;

/** Porta para o envio de SMS (baixo acoplamento com o provedor real). */
public interface ServicoSMS {
    void enviar(String celular, String mensagem);
}
