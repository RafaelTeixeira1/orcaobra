package br.com.orcaobra.api.adaptador.saida.persistencia;

import br.com.orcaobra.api.adaptador.saida.persistencia.entidade.OrcamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface OrcamentoJpaRepository extends JpaRepository<OrcamentoEntity, UUID> {

    List<OrcamentoEntity> findAllByOrderByDataCriacaoDesc();
}
