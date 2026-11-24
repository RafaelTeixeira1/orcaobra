package br.com.orcaobra.aplicacao.fakes;

import br.com.orcaobra.aplicacao.porta.saida.PublicadorDeEventos;
import br.com.orcaobra.dominio.orcamento.eventos.EventoDeDominio;

import java.util.ArrayList;
import java.util.List;

public class PublicadorQueGuarda implements PublicadorDeEventos {

    public final List<EventoDeDominio> publicados = new ArrayList<>();

    @Override
    public void publicar(List<EventoDeDominio> eventos) {
        publicados.addAll(eventos);
    }
}
