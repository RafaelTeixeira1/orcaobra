package br.com.orcaobra.api.adaptador.saida.eventos;

import br.com.orcaobra.dominio.orcamento.eventos.MargemAbaixoDoMinimo;
import br.com.orcaobra.dominio.orcamento.eventos.StatusAlterado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observador dos eventos de domínio. Hoje registra no log; é o ponto de extensão para
 * enviar e-mail/WhatsApp ao engenheiro quando a margem da obra cair.
 */
@Component
class AlertaDeMargemListener {

    private static final Logger log = LoggerFactory.getLogger(AlertaDeMargemListener.class);

    @EventListener
    void margemAbaixo(MargemAbaixoDoMinimo evento) {
        log.warn("ALERTA: orçamento {} com margem projetada de {} (mínimo {})",
                evento.orcamentoId(), evento.margemProjetada(), evento.margemMinima());
    }

    @EventListener
    void statusAlterado(StatusAlterado evento) {
        log.info("Orçamento {} mudou de {} para {}", evento.orcamentoId(), evento.de(), evento.para());
    }
}
