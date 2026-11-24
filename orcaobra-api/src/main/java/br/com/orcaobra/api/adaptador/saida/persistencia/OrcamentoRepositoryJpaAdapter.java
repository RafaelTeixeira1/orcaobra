package br.com.orcaobra.api.adaptador.saida.persistencia;

import br.com.orcaobra.aplicacao.porta.saida.OrcamentoRepository;
import br.com.orcaobra.api.adaptador.saida.persistencia.entidade.OrcamentoEntity;
import br.com.orcaobra.dominio.orcamento.Orcamento;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptador de saída: implementa a porta {@link OrcamentoRepository} usando Spring Data JPA. */
@Component
class OrcamentoRepositoryJpaAdapter implements OrcamentoRepository {

    private final OrcamentoJpaRepository jpa;

    OrcamentoRepositoryJpaAdapter(OrcamentoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Orcamento salvar(Orcamento orcamento) {
        OrcamentoEntity entidade = jpa.findById(orcamento.id())
                .orElseGet(() -> new OrcamentoEntity(orcamento.id()));
        OrcamentoMapper.copiarParaEntidade(orcamento, entidade);
        jpa.save(entidade);
        return orcamento;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Orcamento> buscarPorId(UUID id) {
        return jpa.findById(id).map(OrcamentoMapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Orcamento> listar() {
        return jpa.findAllByOrderByDataCriacaoDesc().stream().map(OrcamentoMapper::paraDominio).toList();
    }
}
