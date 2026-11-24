package br.com.orcaobra.aplicacao.porta.entrada;

import java.util.UUID;

/** Porta de entrada: emissão da proposta comercial em PDF. */
public interface GerarPropostaUseCase {

    record GerarPropostaComando(UUID orcamentoId, String condicoesPagamento, String prazoExecucao,
                                String observacoes) {
    }

    record DocumentoGerado(String nomeArquivo, byte[] conteudo) {
    }

    DocumentoGerado gerar(GerarPropostaComando comando);
}
