package com.poiesis.pedido.springframework.repository.mapper;

import com.poiesis.pedido.domain.entity.ItemPedido;
import com.poiesis.pedido.domain.entity.Pedido;
import com.poiesis.pedido.springframework.repository.entity.CustomizacaoEscolhidaEntity;
import com.poiesis.pedido.springframework.repository.entity.ItemPedidoEntity;
import com.poiesis.pedido.springframework.repository.entity.PedidoEntity;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PedidoPersistenceMapper {
    public ItemPedidoEntity toEntityItem(ItemPedido item) {
        if (item == null) return null;
        var entity = new ItemPedidoEntity(
                item.getProdutoId(),
                item.getNomeProduto(),
                item.getQuantidade(),
                item.getPrecoUnitario());
        entity.setCustomizacoes(item.getCustomizacoes().stream()
                .map(CustomizacaoEscolhidaEntity::new)
                .toList());
        return entity;
    }

    public PedidoEntity toEntity(Pedido pedido) {
        if (pedido == null) return null;
        List<ItemPedidoEntity> itens = pedido.getItens() == null ? List.of()
                : pedido.getItens().stream().map(this::toEntityItem).toList();
        return new PedidoEntity(
                pedido.getId(),
                pedido.getClienteEmail(),
                itens,
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getDataCriacao());
    }

    public ItemPedido toDomainItem(ItemPedidoEntity entity) {
        if (entity == null) return null;
        var item = new ItemPedido(
                entity.getProdutoId(),
                entity.getNomeProduto(),
                entity.getQuantidade(),
                entity.getPrecoUnitario());
        item.setCustomizacoes(entity.getCustomizacoes().stream()
                .map(CustomizacaoEscolhidaEntity::toDomain)
                .toList());
        return item;
    }

    public Pedido toDomain(PedidoEntity entity) {
        if (entity == null) return null;
        List<ItemPedido> itens = entity.getItens() == null ? List.of()
                : entity.getItens().stream().map(this::toDomainItem).toList();
        return new Pedido(
                entity.getId(),
                entity.getClienteEmail(),
                itens,
                entity.getValorTotal(),
                entity.getStatus(),
                entity.getDataCriacao());
    }
}
