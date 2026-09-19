package br.mackenzie.minharefeicao.servico;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/** Pure Fabrication: gera números de protocolo no formato MR-AAAAMMDD-NNNNNN. */
public class GeradorProtocolo {
    private final AtomicInteger sequencia = new AtomicInteger(0);

    public String gerar() {
        String data = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return String.format("MR-%s-%06d", data, sequencia.incrementAndGet());
    }
}
