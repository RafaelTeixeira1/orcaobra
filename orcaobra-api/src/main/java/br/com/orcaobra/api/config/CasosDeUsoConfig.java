package br.com.orcaobra.api.config;

import br.com.orcaobra.aplicacao.porta.entrada.AcompanharMargemUseCase;
import br.com.orcaobra.aplicacao.porta.entrada.CatalogoUseCase;
import br.com.orcaobra.aplicacao.porta.entrada.GerarPropostaUseCase;
import br.com.orcaobra.aplicacao.porta.entrada.GerenciarOrcamentoUseCase;
import br.com.orcaobra.aplicacao.porta.saida.CatalogoDePrecos;
import br.com.orcaobra.aplicacao.porta.saida.GeradorDeDocumentoProposta;
import br.com.orcaobra.aplicacao.porta.saida.OrcamentoRepository;
import br.com.orcaobra.aplicacao.porta.saida.PublicadorDeEventos;
import br.com.orcaobra.aplicacao.servico.AcompanharMargemService;
import br.com.orcaobra.aplicacao.servico.CatalogoService;
import br.com.orcaobra.aplicacao.servico.GerarPropostaService;
import br.com.orcaobra.aplicacao.servico.GerenciarOrcamentoService;
import br.com.orcaobra.aplicacao.servico.PrecificadorDeServico;
import br.com.orcaobra.dominio.proposta.DadosEmpresa;
import br.com.orcaobra.dominio.servico.RegistroDeCalculadoras;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Composition root: é aqui (e só aqui) que o Spring conhece as classes do núcleo.
 * O núcleo continua sem nenhuma anotação de framework (injeção de dependência pelo construtor).
 */
@Configuration
public class CasosDeUsoConfig {

    @Bean
    Clock relogio(OrcaObraProperties props) {
        return Clock.system(ZoneId.of(props.fusoHorario() != null ? props.fusoHorario() : "America/Sao_Paulo"));
    }

    @Bean
    RegistroDeCalculadoras registroDeCalculadoras() {
        return RegistroDeCalculadoras.padrao();
    }

    @Bean
    PrecificadorDeServico precificadorDeServico(RegistroDeCalculadoras registro, CatalogoDePrecos catalogo) {
        return new PrecificadorDeServico(registro, catalogo);
    }

    @Bean
    GerenciarOrcamentoUseCase gerenciarOrcamento(OrcamentoRepository repo, PrecificadorDeServico precificador,
                                                 PublicadorDeEventos publicador, Clock relogio) {
        return new GerenciarOrcamentoService(repo, precificador, publicador, relogio);
    }

    @Bean
    AcompanharMargemUseCase acompanharMargem(OrcamentoRepository repo, PublicadorDeEventos publicador, Clock relogio) {
        return new AcompanharMargemService(repo, publicador, relogio);
    }

    @Bean
    GerarPropostaUseCase gerarProposta(OrcamentoRepository repo, GeradorDeDocumentoProposta gerador,
                                       OrcaObraProperties props, Clock relogio) {
        OrcaObraProperties.Empresa e = props.empresa();
        DadosEmpresa empresa = new DadosEmpresa(e.nome(), e.responsavelTecnico(), e.registroProfissional(),
                e.telefone(), e.email(), e.cidade());
        return new GerarPropostaService(repo, gerador, empresa, relogio);
    }

    @Bean
    CatalogoUseCase catalogo(CatalogoDePrecos catalogo) {
        return new CatalogoService(catalogo);
    }
}
