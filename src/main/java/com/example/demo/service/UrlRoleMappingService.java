package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UrlRoleMapping;
import com.example.demo.repository.UrlRoleMappingRepository;

import java.util.List;

@Service
public class UrlRoleMappingService {
    @Autowired
    private UrlRoleMappingRepository repository;

    public List<UrlRoleMapping> getAll() {
        return repository.findAll();
    }

    public UrlRoleMapping save(UrlRoleMapping mapping) {
        return repository.save(mapping);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
