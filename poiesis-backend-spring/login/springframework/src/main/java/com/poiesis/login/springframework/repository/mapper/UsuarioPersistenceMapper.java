package com.poiesis.login.springframework.repository.mapper;

import com.poiesis.login.domain.entity.Usuario;
import com.poiesis.login.springframework.repository.entity.UsuarioEntity;

public final class UsuarioPersistenceMapper {
    private UsuarioPersistenceMapper() {
    }

    public static Usuario toDomain(UsuarioEntity entity) {
        return new Usuario(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getSenha(),
                entity.getAtivo(),
                entity.getRoles());
    }

    public static UsuarioEntity toEntity(Usuario domain) {
        return new UsuarioEntity(
                domain.getId(),
                domain.getNome(),
                domain.getEmail(),
                domain.getSenha(),
                domain.getAtivo(),
                domain.getRoles());
    }
}
