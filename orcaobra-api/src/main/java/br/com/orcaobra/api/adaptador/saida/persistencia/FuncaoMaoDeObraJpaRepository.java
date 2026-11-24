package br.com.orcaobra.api.adaptador.saida.persistencia;

import br.com.orcaobra.api.adaptador.saida.persistencia.entidade.FuncaoMaoDeObraEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface FuncaoMaoDeObraJpaRepository extends JpaRepository<FuncaoMaoDeObraEntity, String> {
}
