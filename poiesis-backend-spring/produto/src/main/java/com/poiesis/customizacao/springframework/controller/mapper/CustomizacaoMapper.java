package com.poiesis.customizacao.springframework.controller.mapper;

import com.poiesis.customizacao.domain.entity.OpcaoCustomizacao;
import com.poiesis.customizacao.springframework.controller.dto.request.CustomizacaoRequestDTO;
import com.poiesis.customizacao.springframework.controller.dto.response.CustomizacaoResponseDTO;

public class CustomizacaoMapper {

    // RequestDTO -> Domínio
    public static OpcaoCustomizacao toDomain(CustomizacaoRequestDTO dto) {
        return new OpcaoCustomizacao(
            null, 
            dto.produtoId(),
            dto.tipo(),
            dto.nome(),
            dto.precoAdicional(),
            true
        );
    }

    // Domínio -> ResponseDTO
    public static CustomizacaoResponseDTO toResponse(OpcaoCustomizacao domain) {
        return new CustomizacaoResponseDTO(
            domain.id(),
            domain.produtoId(),
            domain.tipo(),
            domain.nome(),
            domain.precoAdicional(),
            domain.ativo()
        );
    }
}
