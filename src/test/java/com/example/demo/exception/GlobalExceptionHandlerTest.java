package com.example.demo.exception;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** 驗證 GlobalExceptionHandler 對各種例外回傳一致的狀態碼與 JSON 格式。 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void notFoundUserReturns404WithJsonBody() throws Exception {
        mvc.perform(get("/api/users/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", equalTo(404)))
                .andExpect(jsonPath("$.message", equalTo("找不到用戶，ID: 9999")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMissingRoleReturns404() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/roles/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", equalTo("找不到角色，ID: 9999")));
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", equalTo(401)));
    }

    @Test
    void duplicateRegisterReturns409() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"x\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", equalTo("用戶名已存在")));
    }

    @Test
    void resetPasswordOfUnknownUserReturns404() throws Exception {
        mvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"newPassword\":\"x\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", equalTo("用戶不存在")));
    }

    @Test
    void checkWithoutLoginReturns401() throws Exception {
        mvc.perform(get("/api/auth/check")).andExpect(status().isUnauthorized());
    }
}
