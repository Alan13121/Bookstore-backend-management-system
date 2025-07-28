// package com.example.demo.controller;

// import com.example.demo.Dto.AuthRequest;
// import com.example.demo.Dto.RegisterRequest;
// import com.example.demo.Dto.ResetPasswordRequest;
// import com.example.demo.service.AuthService;
// import com.example.demo.util.DynamicAuthorizationFilter;
// import com.example.demo.util.JwtTokenProvider;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.ComponentScan;
// import org.springframework.context.annotation.FilterType;
// import org.springframework.http.MediaType;
// import org.springframework.security.core.Authentication;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.Map;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(
//         controllers = AuthController.class,
//         excludeFilters = {
//                 @ComponentScan.Filter(
//                         type = FilterType.ASSIGNABLE_TYPE,
//                         classes = DynamicAuthorizationFilter.class // 排除這個 Filter
//                 )
//         }
// )
// class AuthControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private AuthService authService;

//     @MockBean
//     private JwtTokenProvider jwtTokenProvider;

//     @MockBean
//     private com.example.demo.service.UrlRoleMappingService urlRoleMappingService;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Test
//     void testLogin() throws Exception {
//         when(authService.login(any(AuthRequest.class))).thenReturn("mock-token");

//         AuthRequest request = new AuthRequest();
//         request.setUsername("user");
//         request.setPassword("pass");

//         mockMvc.perform(post("/api/auth/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.token").value("mock-token"));
//     }

//     @Test
//     void testRegister() throws Exception {
//         RegisterRequest request = new RegisterRequest();
//         request.setUsername("newuser");
//         request.setPassword("pwd");

//         // register 是 void，不需額外設定 when()

//         mockMvc.perform(post("/api/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isOk())
//                 .andExpect(content().string("註冊成功"));
//     }

//     @Test
//     void testResetPassword() throws Exception {
//         ResetPasswordRequest request = new ResetPasswordRequest();
//         request.setUsername("user");
//         request.setNewPassword("newpass");

//         mockMvc.perform(post("/api/auth/reset-password")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isOk())
//                 .andExpect(content().string("密碼已重設"));
//     }

//     @Test
//     void testRefreshToken() throws Exception {
//         when(authService.refreshToken(any(Authentication.class))).thenReturn("new-token");

//         mockMvc.perform(get("/api/auth/refresh-token"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.token").value("new-token"));
//     }

//     @Test
//     void testCheckToken() throws Exception {
//         // 這裡 mock 回傳 Map
//         when(authService.checkToken(any(Authentication.class)))
//                 .thenReturn(Map.of("status", "OK"));

//         mockMvc.perform(get("/api/auth/check"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.status").value("OK"));
//     }
// }
