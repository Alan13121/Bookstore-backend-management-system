package com.example.demo.util;

import com.example.demo.entity.UrlRoleMapping;
import com.example.demo.service.UrlRoleMappingService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DynamicAuthorizationFilterTest {

    @Mock
    private UrlRoleMappingService mappingService;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private DynamicAuthorizationFilter filter;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
        response = new MockHttpServletResponse();
        when(mappingService.getAll()).thenReturn(List.of(
                new UrlRoleMapping(1L, "/api/books/.*", "ADMIN,STAFF"),
                new UrlRoleMapping(2L, "/api/users/.*", "ADMIN"),
                new UrlRoleMapping(3L, "/a_office/.*", "A-MANAGER")));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(String... roles) {
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u", null, authorities));
    }

    private MockHttpServletRequest get(String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", uri);
        req.setRequestURI(uri);
        return req;
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/roles/mappings/public", "/favicon.ico", "/js/common.js",
            "/api/auth/login", "/swagger-ui/index.html", "/v3/api-docs/x",
            "/actuator/health", "/book/books.html", "/a_office/a_office.html"})
    void whitelistedPathsPassWithoutLookup(String uri) throws Exception {
        MockHttpServletRequest req = get(uri);

        filter.doFilterInternal(req, response, chain);

        verify(chain).doFilter(req, response);
        verifyNoInteractions(mappingService);
        assertEquals(200, response.getStatus());
    }

    @Test
    void anonymousUserIsForbiddenOnProtectedPath() throws Exception {
        filter.doFilterInternal(get("/api/books/1"), response, chain);

        assertEquals(403, response.getStatus());
        assertEquals("Forbidden: insufficient role", response.getContentAsString());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void userWithMatchingRoleIsAllowed() throws Exception {
        loginAs("STAFF");
        MockHttpServletRequest req = get("/api/books/1");

        filter.doFilterInternal(req, response, chain);

        verify(chain).doFilter(req, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void userWithoutMatchingRoleIsForbidden() throws Exception {
        loginAs("STAFF");

        filter.doFilterInternal(get("/api/users/1"), response, chain);

        assertEquals(403, response.getStatus());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void parentPathIsCoveredByWildcardRule() throws Exception {
        loginAs("STAFF");

        filter.doFilterInternal(get("/api/users"), response, chain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void roleWithHyphenMatches() throws Exception {
        loginAs("A-MANAGER");
        MockHttpServletRequest req = get("/a_office/data");

        filter.doFilterInternal(req, response, chain);

        verify(chain).doFilter(req, response);
    }

    @Test
    void pathWithoutAnyRuleIsAllowedEvenForAnonymous() throws Exception {
        MockHttpServletRequest req = get("/api/unknown");

        filter.doFilterInternal(req, response, chain);

        verify(chain).doFilter(req, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void doubleStarIsTreatedAsRegexWildcard() throws Exception {
        when(mappingService.getAll()).thenReturn(
                List.of(new UrlRoleMapping(9L, "/secret/**", "ADMIN")));
        loginAs("STAFF");

        filter.doFilterInternal(get("/secret/a/b"), response, chain);

        assertEquals(403, response.getStatus());
    }
}
