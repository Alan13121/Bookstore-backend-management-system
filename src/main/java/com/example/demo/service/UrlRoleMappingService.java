package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UrlRoleMapping;
import com.example.demo.repository.UrlRoleMappingRepository;

import java.util.List;

@Service
public class UrlRoleMappingService {
    @Autowired
    private UrlRoleMappingRepository urlRoleMappingRepository;

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
