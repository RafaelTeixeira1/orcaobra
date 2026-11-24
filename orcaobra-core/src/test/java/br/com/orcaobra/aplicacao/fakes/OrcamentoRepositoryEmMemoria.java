package br.com.orcaobra.aplicacao.fakes;

import br.com.orcaobra.aplicacao.porta.saida.OrcamentoRepository;
import br.com.orcaobra.dominio.orcamento.Orcamento;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class OrcamentoRepositoryEmMemoria implements OrcamentoRepository {

    private final Map<UUID, Orcamento> dados = new LinkedHashMap<>();

    @Override
    public Orcamento salvar(Orcamento orcamento) {
        dados.put(orcamento.id(), orcamento);
        return orcamento;
    }

    @Override
    public Optional<Orcamento> buscarPorId(UUID id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public List<Orcamento> listar() {
        return new ArrayList<>(dados.values());
    }
}
