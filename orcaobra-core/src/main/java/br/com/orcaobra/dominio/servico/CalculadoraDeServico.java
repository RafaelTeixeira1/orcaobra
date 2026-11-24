package br.com.orcaobra.dominio.servico;

/**
 * Padrão Strategy: cada tipo de serviço tem a sua regra de consumo de materiais e mão de obra.
 * Para suportar um novo serviço basta criar uma nova implementação e registrá-la
 * (princípio aberto/fechado), sem mexer no restante do sistema.
 */
public interface CalculadoraDeServico {

    TipoServico tipo();

    ComposicaoServico calcular(ParametrosServico parametros);
}
