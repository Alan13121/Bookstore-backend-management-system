package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.UrlRoleMapping;
import com.example.demo.repository.UrlRoleMappingRepository;

@Service
public class UrlRoleMappingService {
    private final UrlRoleMappingRepository urlRoleMappingRepository;

    public UrlRoleMappingService(UrlRoleMappingRepository urlRoleMappingRepository) {
        this.urlRoleMappingRepository = urlRoleMappingRepository;
    }

    public List<UrlRoleMapping> getAll() {
        return urlRoleMappingRepository.findAll();
    }

    public UrlRoleMapping save(UrlRoleMapping mapping) {
        return urlRoleMappingRepository.save(mapping);
    }

    public boolean delete(Long id) {
        if (urlRoleMappingRepository.existsById(id)) {
            urlRoleMappingRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
