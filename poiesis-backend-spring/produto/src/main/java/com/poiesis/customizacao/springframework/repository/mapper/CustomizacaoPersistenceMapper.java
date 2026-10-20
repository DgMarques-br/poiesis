package com.poiesis.customizacao.springframework.repository.mapper;

import com.poiesis.customizacao.domain.entity.OpcaoCustomizacao;
import com.poiesis.customizacao.springframework.repository.entity.CustomizacaoEntity;

public final class CustomizacaoPersistenceMapper {
    private CustomizacaoPersistenceMapper() {
    }

    public static OpcaoCustomizacao toDomain(CustomizacaoEntity entity) {
        return new OpcaoCustomizacao(
                entity.getId(),
                entity.getProdutoId(),
                entity.getTipo(),
                entity.getNome(),
                entity.getPrecoAdicional(),
                entity.getAtivo());
    }

    public static CustomizacaoEntity toEntity(OpcaoCustomizacao domain) {
        return new CustomizacaoEntity(
                domain.id(),
                domain.produtoId(),
                domain.tipo(),
                domain.nome(),
                domain.precoAdicional(),
                domain.ativo());
    }
}
