package com.example.demo.service;

import com.example.demo.Dto.RoleCreateRequest;
import com.example.demo.Dto.RoleDto;
import com.example.demo.entity.Role;
import com.example.demo.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    private RoleDto toDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        return dto;
    }

    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<RoleDto> getRoleById(Integer id) {
        return roleRepository.findById(id)
                .map(this::toDto);
    }

    public RoleDto createRole(RoleCreateRequest request) {
        Role role = new Role();
        role.setName(request.getName());
        return toDto(roleRepository.save(role));
    }

    public RoleDto updateRole(Integer id, RoleCreateRequest request) {
        return roleRepository.findById(id)
            .map(role -> {
                role.setName(request.getName());
                return toDto(roleRepository.save(role));
            })
            .orElseThrow(() -> new RuntimeException("Role not found with id " + id));
    }

    public void deleteRole(Integer id) {
        roleRepository.deleteById(id);
    }

    public ResponseEntity<RoleDto> findRoleResponse(Integer id) {
        return getRoleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<RoleDto> createRoleResponse(RoleCreateRequest request) {
        return ResponseEntity.ok(createRole(request));
    }

    public ResponseEntity<RoleDto> updateRoleResponse(Integer id, RoleCreateRequest request) {
        try {
            return ResponseEntity.ok(updateRole(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Void> deleteRoleResponse(Integer id) {
        deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
