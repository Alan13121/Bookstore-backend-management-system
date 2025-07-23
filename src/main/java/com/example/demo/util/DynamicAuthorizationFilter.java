package com.example.demo.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.entity.UrlRoleMapping;
import com.example.demo.service.UrlRoleMappingService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class DynamicAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private UrlRoleMappingService mappingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        List<UrlRoleMapping> mappings = mappingService.getAll();

        for (UrlRoleMapping mapping : mappings) {
            if (requestURI.matches(mapping.getUrlPattern().replace("**", ".*"))) {
                String[] requiredRoles = mapping.getRoles().split(",");
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth == null || auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(r -> r.replace("ROLE_", "")) // 去掉 ROLE_ 前綴
                        .noneMatch(role -> Arrays.asList(requiredRoles).contains(role))) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Forbidden: insufficient role");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
