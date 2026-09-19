package br.mackenzie.minharefeicao.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Utilitário de apresentação (moeda e datas em pt-BR). */
final class Formatador {
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formatador() { }

    static String moeda(BigDecimal valor) { return MOEDA.format(valor).replace(' ', ' '); }
    static String data(LocalDate data) { return data.format(DATA); }
}
