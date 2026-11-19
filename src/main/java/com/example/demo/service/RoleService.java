package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.RoleCreateRequest;
import com.example.demo.dto.RoleResponse;
import com.example.demo.dto.RoleUpdateRequest;
import com.example.demo.entity.Role;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.RoleRepository;

/**
 * 角色的商業邏輯。
 *
 * <p>{@code @Transactional(readOnly = true)} 加在類別上，表示所有方法預設在「唯讀交易」中執行
 * （Hibernate 會省略變更檢查，效能較好）；會寫入資料的方法再個別標上 {@code @Transactional} 覆蓋成可寫入。
 * 同一個交易中的操作要嘛全部成功、要嘛全部回滾（rollback）。</p>
 */
@Service
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public RoleResponse getRoleById(Integer id) {
        return toResponse(findRole(id));
    }

    @Transactional
    public RoleResponse createRole(RoleCreateRequest request) {
        Role role = new Role();
        role.setName(request.getName());
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse updateRole(Integer id, RoleUpdateRequest request) {
        Role role = findRole(id);
        role.setName(request.getName());
        return toResponse(roleRepository.save(role));
    }

    /** 刪除角色。資料表 user_roles 設有 ON DELETE CASCADE，所以用戶與這個角色的關聯也會一起刪除。 */
    @Transactional
    public void deleteRole(Integer id) {
        if (!roleRepository.existsById(id)) {
            throw notFound(id);
        }
        roleRepository.deleteById(id);
    }

    /** 查不到就丟出 ResourceNotFoundException → 404。 */
    private Role findRole(Integer id) {
        return roleRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private ResourceNotFoundException notFound(Integer id) {
        return new ResourceNotFoundException("找不到角色，ID: " + id);
    }

    /** Entity → DTO。 */
    private RoleResponse toResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        return response;
    }
}
