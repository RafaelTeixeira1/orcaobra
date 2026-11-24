package br.com.orcaobra.api.adaptador.saida.persistencia;

import br.com.orcaobra.api.adaptador.saida.persistencia.entidade.MaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface MaterialJpaRepository extends JpaRepository<MaterialEntity, String> {
}
