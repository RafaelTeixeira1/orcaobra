package br.com.orcaobra.api.adaptador.saida.eventos;

import br.com.orcaobra.aplicacao.porta.saida.PublicadorDeEventos;
import br.com.orcaobra.dominio.orcamento.eventos.EventoDeDominio;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adaptador de saída: repassa os eventos de domínio para o barramento de eventos do Spring. */
@Component
class PublicadorDeEventosSpring implements PublicadorDeEventos {

    private final ApplicationEventPublisher publisher;

    PublicadorDeEventosSpring(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publicar(List<EventoDeDominio> eventos) {
        eventos.forEach(publisher::publishEvent);
    }
}
