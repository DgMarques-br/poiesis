package com.poiesis.login.springframework.repository;

import com.poiesis.login.domain.entity.Usuario;
import com.poiesis.login.domain.repository.UsuarioRepositoryPort;
import com.poiesis.login.springframework.repository.mapper.UsuarioPersistenceMapper;
import com.poiesis.login.springframework.repository.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final SpringDataUsuarioRepository springDataRepository;

    public UsuarioPersistenceAdapter(SpringDataUsuarioRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioEntity entity = UsuarioPersistenceMapper.toEntity(usuario);
        UsuarioEntity saved = springDataRepository.save(entity);
        return UsuarioPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return springDataRepository.findByEmail(email).map(UsuarioPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return springDataRepository.findById(id).map(UsuarioPersistenceMapper::toDomain);
    }

    @Override
    public boolean existePorEmail(String email) {
        return springDataRepository.existsByEmail(email);
    }
}