package br.com.orcaobra.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Teste de integração: HTTP → casos de uso → JPA/H2 → PDF, com o Spring inteiro de pé. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrcamentoApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    private String criarOrcamento() throws Exception {
        String corpo = """
                {"cliente": {"nome": "Maria Souza", "telefone": "(62) 99999-0000"},
                 "obra": {"descricao": "Casa térrea 2 quartos", "cidade": "Ceres-GO", "areaConstruidaM2": 70},
                 "margemMinima": 5, "validadeDias": 15}
                """;
        String resp = mvc.perform(post("/api/orcamentos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resp).get("id").asText();
    }

    private JsonNode adicionar(String id, String corpo) throws Exception {
        String resp = mvc.perform(post("/api/orcamentos/{id}/servicos", id)
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resp);
    }

    private void mudarStatus(String id, String status) throws Exception {
        mvc.perform(patch("/api/orcamentos/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"" + status + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Fluxo completo: criar, adicionar serviços, gerar PDF, executar e acompanhar margem")
    void fluxoCompleto() throws Exception {
        String id = criarOrcamento();

        JsonNode o = adicionar(id, """
                {"tipo": "ALVENARIA", "descricao": "Paredes externas", "quantidade": 10}""");
        assertThat(o.at("/itens/0/custoMateriais").decimalValue()).isEqualByComparingTo("332.55");
        assertThat(o.at("/itens/0/custoMaoDeObra").decimalValue()).isEqualByComparingTo("328.00");

        o = adicionar(id, """
                {"tipo": "PINTURA", "quantidade": 100, "parametros": {"demaos": 3}}""");
        assertThat(o.get("itens")).hasSize(2);

        // relê do banco para garantir que o mapeamento JPA preserva tudo
        mvc.perform(get("/api/orcamentos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.itens[1].parametros.demaos").value(3.0))
                .andExpect(jsonPath("$.itens[0].linhas.length()").value(5));

        mvc.perform(get("/api/orcamentos/{id}/materiais", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("BLOCO_CERAMICO_9X19X19"))
                .andExpect(jsonPath("$[0].quantidadeCompra").value(263));

        byte[] pdf = mvc.perform(get("/api/orcamentos/{id}/proposta.pdf", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        mudarStatus(id, "ENVIADO");
        mudarStatus(id, "APROVADO");
        mudarStatus(id, "EM_EXECUCAO");

        mvc.perform(post("/api/orcamentos/{id}/custos", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\": \"MAO_DE_OBRA\", \"descricao\": \"Empreiteiro\", \"valor\": 2000}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.abaixoDaMargemMinima").value(true));

        mvc.perform(get("/api/orcamentos/{id}", id))
                .andExpect(jsonPath("$.custosRealizados.length()").value(1))
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"));
    }

    @Test
    void regraDeNegocioViraHttp422() throws Exception {
        String id = criarOrcamento();
        mvc.perform(patch("/api/orcamentos/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ENVIADO\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Adicione ao menos um serviço antes de enviar o orçamento"));
    }

    @Test
    void validacaoViraHttp400() throws Exception {
        mvc.perform(post("/api/orcamentos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cliente\": {\"nome\": \"\"}, \"obra\": {\"descricao\": \"x\"}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void orcamentoInexistenteViraHttp404() throws Exception {
        mvc.perform(get("/api/orcamentos/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void catalogoVemDaMigracaoFlyway() throws Exception {
        mvc.perform(get("/api/catalogo/materiais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9));
        mvc.perform(get("/api/servicos"))
                .andExpect(jsonPath("$.length()").value(6));
    }
}
