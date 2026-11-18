package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.PublicUrlRoleMappingResponse;
import com.example.demo.dto.UrlRoleMappingCreateRequest;
import com.example.demo.dto.UrlRoleMappingResponse;
import com.example.demo.dto.UrlRoleMappingUpdateRequest;
import com.example.demo.entity.UrlRoleMapping;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UrlRoleMappingRepository;

@Service
@Transactional(readOnly = true)
public class UrlRoleMappingService {

    private final UrlRoleMappingRepository urlRoleMappingRepository;

    public UrlRoleMappingService(UrlRoleMappingRepository urlRoleMappingRepository) {
        this.urlRoleMappingRepository = urlRoleMappingRepository;
    }

    public List<UrlRoleMappingResponse> getAllMappings() {
        return urlRoleMappingRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PublicUrlRoleMappingResponse> getPublicMappings() {
        return urlRoleMappingRepository.findAll().stream()
                .map(this::toPublicResponse)
                .toList();
    }

    @Transactional
    public UrlRoleMappingResponse createMapping(UrlRoleMappingCreateRequest request) {
        UrlRoleMapping mapping = new UrlRoleMapping();
        mapping.setUrlPattern(request.getUrlPattern());
        mapping.setRoles(request.getRoles());
        return toResponse(urlRoleMappingRepository.save(mapping));
    }

    @Transactional
    public UrlRoleMappingResponse updateMapping(Long id, UrlRoleMappingUpdateRequest request) {
        UrlRoleMapping mapping = urlRoleMappingRepository.findById(id)
                .orElseThrow(() -> notFound(id));
        mapping.setUrlPattern(request.getUrlPattern());
        mapping.setRoles(request.getRoles());
        return toResponse(urlRoleMappingRepository.save(mapping));
    }

    @Transactional
    public void deleteMapping(Long id) {
        if (!urlRoleMappingRepository.existsById(id)) {
            throw notFound(id);
        }
        urlRoleMappingRepository.deleteById(id);
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("找不到權限規則，ID: " + id);
    }

    private UrlRoleMappingResponse toResponse(UrlRoleMapping mapping) {
        UrlRoleMappingResponse response = new UrlRoleMappingResponse();
        response.setId(mapping.getId());
        response.setUrlPattern(mapping.getUrlPattern());
        response.setRoles(mapping.getRoles());
        return response;
    }

    private PublicUrlRoleMappingResponse toPublicResponse(UrlRoleMapping mapping) {
        PublicUrlRoleMappingResponse response = new PublicUrlRoleMappingResponse();
        response.setUrlPattern(mapping.getUrlPattern());
        response.setRoles(mapping.getRoles());
        return response;
    }
}
