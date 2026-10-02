package com.example.demo;

import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 以真實的 JWT 過濾器 + 動態授權過濾器 + H2 資料庫做端到端驗證。 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;

    private String login(String username, String password) throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", username, "password", password));
        String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(json).get("token").asText();
    }

    @Test
    void anonymousCannotReadBooks() throws Exception {
        mvc.perform(get("/api/books")).andExpect(status().isForbidden());
    }

    @Test
    void publicMappingsAreOpen() throws Exception {
        mvc.perform(get("/api/roles/mappings/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].urlPattern").exists());
    }

    @Test
    void adminCanReadBooksAndUsers() throws Exception {
        String token = login("admin", "6969");
        mvc.perform(get("/api/books").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void staffCanReadBooksButNotUsers() throws Exception {
        String token = login("staff", "6969");
        mvc.perform(get("/api/books").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void workerCannotReadBooks() throws Exception {
        String token = login("worker", "6969");
        mvc.perform(get("/api/books").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void departmentPagesFollowRoleRules() throws Exception {
        // 目前的 .html 一律在白名單內，因此先驗證非 .html 的部門路徑
        String staff = login("staff", "6969");   // A-MANAGER
        String worker = login("worker", "6969"); // B-MANAGER
        mvc.perform(get("/a_office/x").header("Authorization", "Bearer " + staff))
                .andExpect(status().isNotFound());
        mvc.perform(get("/a_office/x").header("Authorization", "Bearer " + worker))
                .andExpect(status().isForbidden());
        mvc.perform(get("/b_office/x").header("Authorization", "Bearer " + worker))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidTokenIsTreatedAsAnonymous() throws Exception {
        mvc.perform(get("/api/books").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isForbidden());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", "admin", "password", "wrong"));
        int code = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getStatus();
        assertTrue(code >= 400, "wrong password must not succeed, got " + code);
    }

    @Test
    void registerCreatesStaffUserWhoCanLogin() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "username", "newbie", "password", "pw123456", "fullName", "N", "email", "n@x.com", "phone", "1"));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        assertEquals(java.util.Set.of("STAFF"),
                userRepository.findByUsername("newbie").orElseThrow().getRoles().stream()
                        .map(r -> r.getName()).collect(java.util.stream.Collectors.toSet()));

        String token = login("newbie", "pw123456");
        mvc.perform(get("/api/books").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void refreshAndCheckReturnIdentity() throws Exception {
        String token = login("admin", "6969");
        mvc.perform(get("/api/auth/check").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
        mvc.perform(get("/api/auth/refresh-token").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void adminCanDeleteBookButStaffCannotReachUserDelete() throws Exception {
        String admin = login("admin", "6969");
        mvc.perform(delete("/api/books/3").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/books/3").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());

        String staff = login("staff", "6969");
        mvc.perform(delete("/api/users/1").header("Authorization", "Bearer " + staff))
                .andExpect(status().isForbidden());
    }

    /** 記錄已知風險：reset-password 不需登入，也不驗證身分（僅描述現況，非期望行為）。 */
    @Test
    void knownRisk_resetPasswordRequiresNoAuthentication() throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", "worker", "newPassword", "hacked1"));
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        login("worker", "hacked1");
    }
}
