package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> getRoleById(Integer id) {
        return roleRepository.findById(id);
    }

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    public Role updateRole(Integer id, Role updatedRole) {
        return roleRepository.findById(id)
            .map(role -> {
                role.setName(updatedRole.getName());
                return roleRepository.save(role);
            })
            .orElseThrow(() -> new RuntimeException("Role not found with id " + id));
    }

    public void deleteRole(Integer id) {
        roleRepository.deleteById(id);
    }
}
