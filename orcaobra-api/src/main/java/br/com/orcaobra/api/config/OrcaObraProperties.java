package br.com.orcaobra.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configurações em application.yml (prefixo "orcaobra"). */
@ConfigurationProperties(prefix = "orcaobra")
public record OrcaObraProperties(String fusoHorario, Empresa empresa) {

    public record Empresa(String nome, String responsavelTecnico, String registroProfissional,
                          String telefone, String email, String cidade) {
    }
}
