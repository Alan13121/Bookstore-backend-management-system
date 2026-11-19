package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.UrlRoleMapping;
import com.example.demo.repository.UrlRoleMappingRepository;

/**
 * 動態權限規則（url_role_mapping）的業務邏輯層。
 *
 * <p>{@code @Service} 本質上和 {@code @Component} 一樣，會被註冊成 Bean，
 * 只是在語意上標示「這是業務邏輯層」。</p>
 *
 * <p>分層架構：Controller（接收請求）→ Service（業務邏輯）→ Repository（存取資料庫）。
 * 這個 Service 目前只是轉呼叫 Repository，但之後若要加入快取或驗證規則格式，就會寫在這裡。</p>
 */
@Service
public class UrlRoleMappingService {
    private final UrlRoleMappingRepository urlRoleMappingRepository;

    public UrlRoleMappingService(UrlRoleMappingRepository urlRoleMappingRepository) {
        this.urlRoleMappingRepository = urlRoleMappingRepository;
    }

    /** 取得所有規則（DynamicAuthorizationFilter 每個 request 都會呼叫）。 */
    public List<UrlRoleMapping> getAll() {
        return urlRoleMappingRepository.findAll();
    }

    /** 新增或更新規則：mapping 的 id 為 null 時新增，有值時更新。 */
    public UrlRoleMapping save(UrlRoleMapping mapping) {
        return urlRoleMappingRepository.save(mapping);
    }

    /** 刪除規則；回傳 false 代表該 id 不存在。 */
    public boolean delete(Long id) {
        if (urlRoleMappingRepository.existsById(id)) {
            urlRoleMappingRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
