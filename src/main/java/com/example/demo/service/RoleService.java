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

    @Transactional
    public void deleteRole(Integer id) {
        if (!roleRepository.existsById(id)) {
            throw notFound(id);
        }
        roleRepository.deleteById(id);
    }

    private Role findRole(Integer id) {
        return roleRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private ResourceNotFoundException notFound(Integer id) {
        return new ResourceNotFoundException("找不到角色，ID: " + id);
    }

    private RoleResponse toResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        return response;
    }
}
