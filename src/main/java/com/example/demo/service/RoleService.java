package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.RoleCreateRequest;
import com.example.demo.dto.RoleDto;
import com.example.demo.entity.Role;
import com.example.demo.repository.RoleRepository;

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
            .orElseThrow(() -> new IllegalArgumentException("找不到角色，ID: " + id));
    }

    public boolean deleteRole(Integer id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
