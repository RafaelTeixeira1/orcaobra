package br.com.orcaobra.aplicacao.porta.saida;

import br.com.orcaobra.dominio.orcamento.eventos.EventoDeDominio;

import java.util.List;

/** Porta de saída: entrega eventos de domínio aos interessados (Observer). */
public interface PublicadorDeEventos {

    void publicar(List<EventoDeDominio> eventos);
}
