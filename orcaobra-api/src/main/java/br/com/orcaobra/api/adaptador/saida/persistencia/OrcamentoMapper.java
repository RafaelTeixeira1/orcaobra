package br.com.orcaobra.api.adaptador.saida.persistencia;

import br.com.orcaobra.api.adaptador.saida.persistencia.entidade.CustoRealizadoEntity;
import br.com.orcaobra.api.adaptador.saida.persistencia.entidade.ItemOrcamentoEntity;
import br.com.orcaobra.api.adaptador.saida.persistencia.entidade.LinhaDeCustoEmbeddable;
import br.com.orcaobra.api.adaptador.saida.persistencia.entidade.OrcamentoEntity;
import br.com.orcaobra.dominio.comum.Dinheiro;
import br.com.orcaobra.dominio.comum.Percentual;
import br.com.orcaobra.dominio.orcamento.Cliente;
import br.com.orcaobra.dominio.orcamento.ComposicaoBdi;
import br.com.orcaobra.dominio.orcamento.CustoRealizado;
import br.com.orcaobra.dominio.orcamento.ItemOrcamento;
import br.com.orcaobra.dominio.orcamento.LinhaDeCusto;
import br.com.orcaobra.dominio.orcamento.Obra;
import br.com.orcaobra.dominio.orcamento.Orcamento;
import br.com.orcaobra.dominio.servico.ParametrosServico;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Converte entre o agregado de domínio e o modelo JPA. */
final class OrcamentoMapper {

    private OrcamentoMapper() {
    }

    static Orcamento paraDominio(OrcamentoEntity e) {
        List<ItemOrcamento> itens = e.getItens().stream()
                .map(i -> new ItemOrcamento(i.getId(), i.getTipo(), i.getDescricao(),
                        new ParametrosServico(i.getQuantidade(), Map.copyOf(i.getParametros())),
                        i.getLinhas().stream().map(OrcamentoMapper::linhaParaDominio).toList()))
                .toList();
        List<CustoRealizado> custos = e.getCustos().stream()
                .map(c -> new CustoRealizado(c.getId(), c.getData(), c.getCategoria(), c.getDescricao(),
                        Dinheiro.de(c.getValor())))
                .toList();
        return Orcamento.reconstituir(e.getId(), e.getDataCriacao(),
                new Cliente(e.getClienteNome(), e.getClienteDocumento(), e.getClienteEmail(), e.getClienteTelefone()),
                new Obra(e.getObraDescricao(), e.getObraEndereco(), e.getObraCidade(), e.getObraAreaM2()),
                new ComposicaoBdi(new Percentual(e.getBdiAdministracao()), new Percentual(e.getBdiSeguro()),
                        new Percentual(e.getBdiRisco()), new Percentual(e.getBdiDespesasFinanceiras()),
                        new Percentual(e.getBdiLucro()), new Percentual(e.getBdiImpostos())),
                new Percentual(e.getMargemMinima()), e.getValidadeDias(), e.getStatus(), itens, custos);
    }

    /**
     * Copia o estado do domínio para a entidade. Itens e custos são imutáveis no domínio,
     * então basta remover os que saíram e incluir os novos (sem apagar e reinserir tudo).
     */
    static void copiarParaEntidade(Orcamento o, OrcamentoEntity e) {
        e.setDataCriacao(o.dataCriacao());
        e.setClienteNome(o.cliente().nome());
        e.setClienteDocumento(o.cliente().documento());
        e.setClienteEmail(o.cliente().email());
        e.setClienteTelefone(o.cliente().telefone());
        e.setObraDescricao(o.obra().descricao());
        e.setObraEndereco(o.obra().endereco());
        e.setObraCidade(o.obra().cidade());
        e.setObraAreaM2(o.obra().areaConstruidaM2());
        ComposicaoBdi bdi = o.bdi();
        e.setBdiAdministracao(bdi.administracaoCentral().fracao());
        e.setBdiSeguro(bdi.seguroEGarantia().fracao());
        e.setBdiRisco(bdi.risco().fracao());
        e.setBdiDespesasFinanceiras(bdi.despesasFinanceiras().fracao());
        e.setBdiLucro(bdi.lucro().fracao());
        e.setBdiImpostos(bdi.impostos().fracao());
        e.setMargemMinima(o.margemMinima().fracao());
        e.setValidadeDias(o.validadeDias());
        e.setStatus(o.status());

        // Itens
        Set<UUID> idsItens = o.itens().stream().map(ItemOrcamento::id).collect(Collectors.toSet());
        e.getItens().removeIf(i -> !idsItens.contains(i.getId()));
        Map<UUID, ItemOrcamentoEntity> existentes = e.getItens().stream()
                .collect(Collectors.toMap(ItemOrcamentoEntity::getId, i -> i));
        int ordem = 0;
        for (ItemOrcamento item : o.itens()) {
            ItemOrcamentoEntity ie = existentes.get(item.id());
            if (ie == null) {
                ie = new ItemOrcamentoEntity(item.id(), e, ordem, item.tipo(), item.descricao(),
                        item.parametros().quantidade(), item.parametros().extras(),
                        item.linhas().stream().map(OrcamentoMapper::linhaParaEntidade).toList());
                e.getItens().add(ie);
            } else {
                ie.setOrdem(ordem);
            }
            ordem++;
        }

        // Custos realizados (somente inclusão)
        Set<UUID> idsCustos = e.getCustos().stream().map(CustoRealizadoEntity::getId).collect(Collectors.toSet());
        for (CustoRealizado c : o.custosRealizados()) {
            if (!idsCustos.contains(c.id())) {
                e.getCustos().add(new CustoRealizadoEntity(c.id(), e, c.data(), c.categoria(), c.descricao(),
                        c.valor().valor()));
            }
        }
    }

    private static LinhaDeCusto linhaParaDominio(LinhaDeCustoEmbeddable l) {
        return new LinhaDeCusto(l.getTipo(), l.getCodigo(), l.getDescricao(), l.getUnidade(), l.getQuantidade(),
                Dinheiro.de(l.getPrecoUnitario()));
    }

    private static LinhaDeCustoEmbeddable linhaParaEntidade(LinhaDeCusto l) {
        return new LinhaDeCustoEmbeddable(l.tipo(), l.codigo(), l.descricao(), l.unidade(), l.quantidade(),
                l.precoUnitario().valor());
    }
}
