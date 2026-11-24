package br.com.orcaobra.api.adaptador.entrada.web.dto;

import br.com.orcaobra.dominio.comum.Percentual;
import br.com.orcaobra.dominio.orcamento.Cliente;
import br.com.orcaobra.dominio.orcamento.ComposicaoBdi;
import br.com.orcaobra.dominio.orcamento.Obra;
import br.com.orcaobra.dominio.orcamento.StatusOrcamento;
import br.com.orcaobra.dominio.orcamento.TipoCusto;
import br.com.orcaobra.dominio.servico.TipoServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Corpos das requisições HTTP. Percentuais chegam em pontos (7.4 = 7,4%). */
public final class Requisicoes {

    private Requisicoes() {
    }

    public record ClienteDto(@NotBlank String nome, String documento, String email, String telefone) {
        public Cliente paraDominio() {
            return new Cliente(nome, documento, email, telefone);
        }
    }

    public record ObraDto(@NotBlank String descricao, String endereco, String cidade,
                          @Positive BigDecimal areaConstruidaM2) {
        public Obra paraDominio() {
            return new Obra(descricao, endereco, cidade, areaConstruidaM2);
        }
    }

    public record BdiDto(@NotNull @PositiveOrZero BigDecimal administracaoCentral,
                         @NotNull @PositiveOrZero BigDecimal seguroEGarantia,
                         @NotNull @PositiveOrZero BigDecimal risco,
                         @NotNull @PositiveOrZero BigDecimal despesasFinanceiras,
                         @NotNull @PositiveOrZero BigDecimal lucro,
                         @NotNull @PositiveOrZero BigDecimal impostos) {
        public ComposicaoBdi paraDominio() {
            return new ComposicaoBdi(Percentual.de(administracaoCentral), Percentual.de(seguroEGarantia),
                    Percentual.de(risco), Percentual.de(despesasFinanceiras), Percentual.de(lucro),
                    Percentual.de(impostos));
        }

        public static BdiDto de(ComposicaoBdi b) {
            return new BdiDto(b.administracaoCentral().emPontos(), b.seguroEGarantia().emPontos(),
                    b.risco().emPontos(), b.despesasFinanceiras().emPontos(), b.lucro().emPontos(),
                    b.impostos().emPontos());
        }
    }

    public record CriarOrcamentoRequest(@NotNull @Valid ClienteDto cliente, @NotNull @Valid ObraDto obra,
                                        @Valid BdiDto bdi, @PositiveOrZero BigDecimal margemMinima,
                                        @Positive Integer validadeDias) {
    }

    public record AdicionarServicoRequest(@NotNull TipoServico tipo, String descricao,
                                          @NotNull @Positive BigDecimal quantidade,
                                          Map<String, BigDecimal> parametros) {
    }

    public record CondicoesRequest(@NotNull @Valid BdiDto bdi, @NotNull @PositiveOrZero BigDecimal margemMinima,
                                   @NotNull @Positive Integer validadeDias) {
    }

    public record AlterarStatusRequest(@NotNull StatusOrcamento status) {
    }

    public record LancarCustoRequest(LocalDate data, @NotNull TipoCusto categoria, @NotBlank String descricao,
                                     @NotNull @Positive BigDecimal valor) {
    }

    public record AtualizarPrecoRequest(@NotNull @PositiveOrZero BigDecimal valor) {
    }
}
