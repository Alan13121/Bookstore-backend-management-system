package com.example.demo.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.service.CustomUserDetailsService;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String token = null;
        String username = null;

        System.out.println("[DEBUG] JwtAuthenticationFilter triggered for URI: " + request.getRequestURI());


        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                username = jwtTokenProvider.getUsernameFromToken(token);

                System.out.println("[DEBUG] Valid JWT detected.");
                System.out.println("[DEBUG] Token: " + token);
                System.out.println("[DEBUG] Username from token: " + username);
            }
        }


        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            List<String> roles = jwtTokenProvider.getRolesFromToken(token);

            System.out.println("[DEBUG] Roles from JWT: " + roles);

            List<GrantedAuthority> authorities = roles.stream()
                                                    .map(SimpleGrantedAuthority::new)
                                                    .map(authority -> (GrantedAuthority) authority)
                                                    .toList();


            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
        }



        filterChain.doFilter(request, response);
    }
}

