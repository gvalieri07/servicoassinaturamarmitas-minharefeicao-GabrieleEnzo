package br.mackenzie.minharefeicao.persistencia;

import br.mackenzie.minharefeicao.dominio.*;
import java.math.BigDecimal;
import java.util.EnumSet;

import static br.mackenzie.minharefeicao.dominio.PreferenciaAlimentar.*;

/** Carga inicial (pré-condição: planos disponíveis e cardápio atualizado). */
public final class DadosIniciais {
    private DadosIniciais() { }

    public static RepositorioPlanos criarPlanos() {
        RepositorioPlanos repo = new RepositorioPlanos();
        repo.salvar(new PlanoAssinatura(1, "Essencial", 5, Periodicidade.SEMANAL, new BigDecimal("149.90")));
        repo.salvar(new PlanoAssinatura(2, "Equilíbrio", 10, Periodicidade.QUINZENAL, new BigDecimal("279.90")));
        repo.salvar(new PlanoAssinatura(3, "Família", 20, Periodicidade.MENSAL, new BigDecimal("519.90")));
        return repo;
    }

    public static Cardapio criarCardapio() {
        Cardapio c = new Cardapio();
        // Pratos principais
        c.adicionarItem(new PratoPrincipal("P1", "Frango grelhado ao limão", 350, EnumSet.of(TRADICIONAL, SEM_LACTOSE)));
        c.adicionarItem(new PratoPrincipal("P2", "Escondidinho de carne seca", 400, EnumSet.of(TRADICIONAL)));
        c.adicionarItem(new PratoPrincipal("P3", "Lasanha de berinjela", 380, EnumSet.of(TRADICIONAL, VEGETARIANA)));
        c.adicionarItem(new PratoPrincipal("P4", "Curry de grão-de-bico", 350, EnumSet.of(TRADICIONAL, VEGETARIANA, SEM_LACTOSE)));
        c.adicionarItem(new PratoPrincipal("P5", "Tilápia com crosta de ervas", 330, EnumSet.of(TRADICIONAL, SEM_LACTOSE)));
        // Acompanhamentos
        c.adicionarItem(new Acompanhamento("A1", "Arroz integral", EnumSet.of(TRADICIONAL, VEGETARIANA, SEM_LACTOSE)));
        c.adicionarItem(new Acompanhamento("A2", "Purê de batata", EnumSet.of(TRADICIONAL, VEGETARIANA)));
        c.adicionarItem(new Acompanhamento("A3", "Legumes no vapor", EnumSet.of(TRADICIONAL, VEGETARIANA, SEM_LACTOSE)));
        c.adicionarItem(new Acompanhamento("A4", "Farofa de bacon", EnumSet.of(TRADICIONAL, SEM_LACTOSE)));
        // Sobremesas
        c.adicionarItem(new Sobremesa("S1", "Mousse de maracujá", EnumSet.of(TRADICIONAL, VEGETARIANA)));
        c.adicionarItem(new Sobremesa("S2", "Salada de frutas", EnumSet.of(TRADICIONAL, VEGETARIANA, SEM_LACTOSE)));
        c.adicionarItem(new Sobremesa("S3", "Brownie vegano", EnumSet.of(TRADICIONAL, VEGETARIANA, SEM_LACTOSE)));
        return c;
    }
}
