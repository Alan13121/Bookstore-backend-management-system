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

/**
 * 動態權限規則的商業邏輯。
 *
 * <p>{@code @Transactional(readOnly = true)} 加在類別上，表示所有方法預設在「唯讀交易」中執行
 * （Hibernate 會省略變更檢查，效能較好）；會寫入資料的方法再個別標上 {@code @Transactional} 覆蓋成可寫入。
 * 同一個交易中的操作要嘛全部成功、要嘛全部回滾（rollback）。</p>
 */
@Service
@Transactional(readOnly = true)
public class UrlRoleMappingService {

    private final UrlRoleMappingRepository urlRoleMappingRepository;

    public UrlRoleMappingService(UrlRoleMappingRepository urlRoleMappingRepository) {
        this.urlRoleMappingRepository = urlRoleMappingRepository;
    }

    /** 取得所有規則。DynamicAuthorizationFilter 每個 request 都會呼叫這個方法。 */
    public List<UrlRoleMappingResponse> getAllMappings() {
        return urlRoleMappingRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /** 給前端選單用的公開版規則（不含 id）。 */
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
