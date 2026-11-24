package br.com.orcaobra.dominio.proposta;

/** Quem emite a proposta: empresa ou profissional, com o registro no CREA/CAU. */
public record DadosEmpresa(String nome, String responsavelTecnico, String registroProfissional,
                           String telefone, String email, String cidade) {
}
