package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserEntityTest {

    @Test
    void authoritiesArePrefixedWithRole() {
        User user = new User();
        user.setRoles(Set.of(new Role(1, "ADMIN"), new Role(2, "STAFF")));

        Set<String> names = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        assertEquals(Set.of("ROLE_ADMIN", "ROLE_STAFF"), names);
    }

    @Test
    void noRolesMeansNoAuthorities() {
        assertTrue(new User().getAuthorities().isEmpty());
    }

    @Test
    void enabledFlagHandlesNullAndValues() {
        User user = new User();
        assertFalse(user.isEnabled());
        user.setEnabled(true);
        assertTrue(user.isEnabled());
        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }

    @Test
    void accountStatusFlagsAreAlwaysTrue() {
        User user = new User();
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
    }
}
