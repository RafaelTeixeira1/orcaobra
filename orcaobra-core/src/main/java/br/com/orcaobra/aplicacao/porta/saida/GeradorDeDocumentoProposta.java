package br.com.orcaobra.aplicacao.porta.saida;

import br.com.orcaobra.dominio.proposta.Proposta;

/** Porta de saída: transforma a proposta em um documento (PDF). */
public interface GeradorDeDocumentoProposta {

    byte[] gerar(Proposta proposta);
}
