package com.poiesis.login.springframework.controller.mapper;

import com.poiesis.login.domain.entity.Usuario;
import com.poiesis.login.springframework.controller.dto.request.RegisterRequestDTO;
import com.poiesis.login.springframework.controller.dto.response.UsuarioResponseDTO;

public class UsuarioMapper {

    public static Usuario toDomain(RegisterRequestDTO dto) {
        return new Usuario(null, dto.getNome(), dto.getEmail(), dto.getSenha(), true, null);
    }

    public static UsuarioResponseDTO toResponse(Usuario domain) {
        return new UsuarioResponseDTO(domain.getId(), domain.getNome(), domain.getEmail(), domain.getRoles());
    }
}
