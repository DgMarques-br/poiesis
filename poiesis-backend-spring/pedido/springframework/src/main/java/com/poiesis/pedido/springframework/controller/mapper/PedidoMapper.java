package com.poiesis.pedido.springframework.controller.mapper;

import com.poiesis.pedido.domain.entity.ItemPedido;
import com.poiesis.pedido.domain.entity.Pedido;
import com.poiesis.pedido.springframework.controller.dto.request.ItemPedidoRequestDTO;
import com.poiesis.pedido.springframework.controller.dto.request.PedidoRequestDTO;
import com.poiesis.pedido.springframework.controller.dto.response.ItemPedidoResponseDTO;
import com.poiesis.pedido.springframework.controller.dto.response.PedidoResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PedidoMapper {

    // --- Mapeamentos do DTO de Request para Domínio ---
    public ItemPedido toDomainItem(ItemPedidoRequestDTO dto) {
        if (dto == null) return null;
        var item = new ItemPedido(dto.getProdutoId(), null, dto.getQuantidade(), null);
        item.setCustomizacaoIds(dto.getCustomizacaoIds());
        return item;
    }

    public List<ItemPedido> toDomainItemList(List<ItemPedidoRequestDTO> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream().map(this::toDomainItem).collect(Collectors.toList());
    }

    // --- Mapeamentos do Domínio para DTO de Response ---
    public ItemPedidoResponseDTO toResponseItemDTO(ItemPedido item) {
        if (item == null) return null;
        var response = new ItemPedidoResponseDTO(
                item.getProdutoId(),
                item.getNomeProduto(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal()
        );
        response.setCustomizacoes(item.getCustomizacoes());
        return response;
    }

    public PedidoResponseDTO toResponseDTO(Pedido pedido) {
        if (pedido == null) return null;
        List<ItemPedidoResponseDTO> itensDTO = pedido.getItens() == null ? List.of() :
                pedido.getItens().stream().map(this::toResponseItemDTO).collect(Collectors.toList());

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getClienteEmail(),
                itensDTO,
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getDataCriacao()
        );
    }

}
