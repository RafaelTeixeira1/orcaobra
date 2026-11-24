package br.com.orcaobra.dominio.orcamento.eventos;

import br.com.orcaobra.dominio.comum.Percentual;

import java.time.Instant;
import java.util.UUID;

/** Disparado quando a margem projetada cruza para baixo da margem mínima definida. */
public record MargemAbaixoDoMinimo(UUID orcamentoId, Percentual margemProjetada, Percentual margemMinima,
                                   Instant ocorridoEm) implements EventoDeDominio {
}
