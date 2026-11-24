package br.com.orcaobra.dominio.orcamento.eventos;

import java.time.Instant;
import java.util.UUID;

/**
 * Padrão Observer (eventos de domínio): o agregado registra o que aconteceu e a camada
 * de aplicação publica para quem tiver interesse (alertas, log, e-mail...).
 */
public sealed interface EventoDeDominio permits MargemAbaixoDoMinimo, StatusAlterado {

    UUID orcamentoId();

    Instant ocorridoEm();
}
