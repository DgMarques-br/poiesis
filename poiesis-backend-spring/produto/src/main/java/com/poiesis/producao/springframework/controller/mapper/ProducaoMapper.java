package com.poiesis.producao.springframework.controller.mapper;

import com.poiesis.producao.domain.entity.OrdemProducao;
import com.poiesis.producao.springframework.controller.dto.response.OrdemProducaoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ProducaoMapper {

    public OrdemProducaoResponseDTO toResponseDTO(OrdemProducao domain) {
        if (domain == null) return null;
        return new OrdemProducaoResponseDTO(
                domain.getId(),
                domain.getPedidoId(),
                domain.getClienteEmail(),
                domain.getStatus(),
                domain.getDataInicio(),
                domain.getDataAtualizacao()
        );
    }

}