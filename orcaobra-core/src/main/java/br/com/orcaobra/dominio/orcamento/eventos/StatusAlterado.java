package br.com.orcaobra.dominio.orcamento.eventos;

import br.com.orcaobra.dominio.orcamento.StatusOrcamento;

import java.time.Instant;
import java.util.UUID;

public record StatusAlterado(UUID orcamentoId, StatusOrcamento de, StatusOrcamento para, Instant ocorridoEm)
        implements EventoDeDominio {
}
