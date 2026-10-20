package com.poiesis.producao.springframework.repository.mapper;

import com.poiesis.producao.domain.entity.OrdemProducao;
import com.poiesis.producao.springframework.repository.entity.OrdemProducaoEntity;
import org.springframework.stereotype.Component;

@Component
public class OrdemProducaoPersistenceMapper {
    public OrdemProducaoEntity toEntity(OrdemProducao domain) {
        if (domain == null) return null;
        return new OrdemProducaoEntity(
                domain.getId(),
                domain.getPedidoId(),
                domain.getClienteEmail(),
                domain.getStatus(),
                domain.getDataInicio(),
                domain.getDataAtualizacao());
    }

    public OrdemProducao toDomain(OrdemProducaoEntity entity) {
        if (entity == null) return null;
        return new OrdemProducao(
                entity.getId(),
                entity.getPedidoId(),
                entity.getClienteEmail(),
                entity.getStatus(),
                entity.getDataInicio(),
                entity.getDataAtualizacao());
    }
}
