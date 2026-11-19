package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.RoleCreateRequest;
import com.example.demo.dto.RoleDto;
import com.example.demo.entity.Role;
import com.example.demo.repository.RoleRepository;

/** 角色的業務邏輯層。 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /** Entity → DTO。 */
    private RoleDto toDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        return dto;
    }

    /** 查詢所有角色。 */
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** 依 id 查詢角色，查不到回傳 Optional.empty()。 */
    public Optional<RoleDto> getRoleById(Integer id) {
        return roleRepository.findById(id)
                .map(this::toDto);
    }

    /** 新增角色。注意角色名稱不需要加 "ROLE_" 前綴（例如直接傳 MANAGER）。 */
    public RoleDto createRole(RoleCreateRequest request) {
        Role role = new Role();
        role.setName(request.getName());
        return toDto(roleRepository.save(role));
    }

    /**
     * 更新角色名稱；找不到時丟出 IllegalArgumentException，由 GlobalExceptionHandler 轉成 404。
     * （與 getRoleById 回傳 Optional 是兩種不同的「找不到」處理方式，可以比較看看。）
     */
    public RoleDto updateRole(Integer id, RoleCreateRequest request) {
        return roleRepository.findById(id)
            .map(role -> {
                role.setName(request.getName());
                return toDto(roleRepository.save(role));
            })
            .orElseThrow(() -> new IllegalArgumentException("找不到角色，ID: " + id));
    }

    /**
     * 刪除角色；回傳 false 代表 id 不存在。
     * 資料表 user_roles 設有 ON DELETE CASCADE，所以會一併移除用戶與此角色的關聯。
     */
    public boolean deleteRole(Integer id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
