package com.poiesis.customizacao.springframework.controller;

import com.poiesis.customizacao.domain.entity.OpcaoCustomizacao;
import com.poiesis.customizacao.domain.service.CustomizacaoDomainService;
import com.poiesis.customizacao.springframework.controller.mapper.CustomizacaoMapper;
import com.poiesis.customizacao.springframework.controller.dto.request.CustomizacaoRequestDTO;
import com.poiesis.customizacao.springframework.controller.dto.response.CustomizacaoResponseDTO;
import com.poiesis.catalogo.domain.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/customizacoes")
public class CustomizacaoController {

    private final CustomizacaoDomainService domainService;
    private final CatalogoService catalogoService;

    public CustomizacaoController(CustomizacaoDomainService domainService, CatalogoService catalogoService) {
        this.domainService = domainService;
        this.catalogoService = catalogoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public ResponseEntity<CustomizacaoResponseDTO> criar(@Valid @RequestBody CustomizacaoRequestDTO dto) {
        catalogoService.buscarPorId(dto.produtoId());

        OpcaoCustomizacao domain = CustomizacaoMapper.toDomain(dto);
        OpcaoCustomizacao criada = domainService.criarOpcao(domain);
        CustomizacaoResponseDTO response = CustomizacaoMapper.toResponse(criada);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<CustomizacaoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody CustomizacaoRequestDTO dto) {
        catalogoService.buscarPorId(dto.produtoId());
        return ResponseEntity.ok(CustomizacaoMapper.toResponse(domainService.atualizarOpcao(id, CustomizacaoMapper.toDomain(dto))));
    }

    @GetMapping("/produto/{produtoId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<CustomizacaoResponseDTO>> listarPorProduto(@PathVariable Long produtoId) {
        List<CustomizacaoResponseDTO> lista = domainService.listarPorProduto(produtoId)
                .stream()
                .map(CustomizacaoMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        return domainService.inativarOpcao(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
