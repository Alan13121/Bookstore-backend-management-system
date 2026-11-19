package com.example.demo.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    private final Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Level originalLevel;

    @BeforeEach
    void setUp() {
        originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        logger.setLevel(originalLevel);
    }

    private List<String> logs() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    private MockHttpServletRequest jsonRequest(String method, String uri, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setContentType("application/json");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }

    /** 模擬 Controller：讀取 request body，回傳 JSON。 */
    private FilterChain controllerReturning(String responseJson, AtomicReference<String> seenRequestId) {
        return (req, res) -> {
            seenRequestId.set(MDC.get(RequestLoggingFilter.MDC_KEY));
            req.getInputStream().readAllBytes();
            res.setContentType("application/json");
            res.getOutputStream().write(responseJson.getBytes(StandardCharsets.UTF_8));
        };
    }

    @Test
    void logsRequestAndResponseWithMaskedSecrets() throws Exception {
        MockHttpServletRequest request =
                jsonRequest("POST", "/api/auth/login", "{\"username\":\"admin\",\"password\":\"6969\"}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                controllerReturning("{\"token\":\"eyJhbGciOi.secret\"}", new AtomicReference<>()));

        String all = String.join("\n", logs());
        assertTrue(all.contains("→ POST /api/auth/login"));
        assertTrue(all.contains("← 200 POST /api/auth/login"));
        assertTrue(all.contains("\"username\":\"admin\""));
        assertTrue(all.contains("\"password\":\"***\""));
        assertTrue(all.contains("\"token\":\"***\""));
        assertFalse(all.contains("6969"));
        assertFalse(all.contains("eyJhbGciOi.secret"));
    }

    @Test
    void responseBodyStillReachesClient() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest("GET", "/api/books"), response,
                controllerReturning("[{\"id\":1,\"title\":\"書名\"}]", new AtomicReference<>()));

        assertEquals("[{\"id\":1,\"title\":\"書名\"}]", response.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void putsRequestIdIntoMdcAndHeaderThenClearsIt() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();

        filter.doFilter(new MockHttpServletRequest("GET", "/api/books"), response,
                controllerReturning("[]", seen));

        assertNotNull(seen.get());
        assertEquals(seen.get(), response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER));
        assertNull(MDC.get(RequestLoggingFilter.MDC_KEY));
    }

    @Test
    void reusesValidRequestIdFromClient() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/books");
        request.addHeader(RequestLoggingFilter.REQUEST_ID_HEADER, "client-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, controllerReturning("[]", new AtomicReference<>()));

        assertEquals("client-123", response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER));
    }

    @Test
    void replacesInvalidRequestIdFromClient() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/books");
        request.addHeader(RequestLoggingFilter.REQUEST_ID_HEADER, "bad\nINFO fake log line");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, controllerReturning("[]", new AtomicReference<>()));

        String id = response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER);
        assertNotEquals("bad\nINFO fake log line", id);
        assertEquals(8, id.length());
    }

    @Test
    void skipsStaticResources() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest("GET", "/book/books.html"), response,
                (req, res) -> { });

        assertTrue(logs().isEmpty());
        assertNull(response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER));
    }

    @Test
    void truncatesLongBody() {
        String body = "\"" + "a".repeat(5000) + "\"";

        String logged = RequestLoggingFilter.toLoggableBody(body.getBytes(StandardCharsets.UTF_8), "application/json");

        assertTrue(logged.length() < 2100);
        assertTrue(logged.contains("已截斷"));
    }

    @Test
    void describesBinaryBodyInsteadOfPrintingIt() {
        String logged = RequestLoggingFilter.toLoggableBody(new byte[] {1, 2, 3}, "image/png");

        assertEquals("[image/png, 3 bytes]", logged);
    }
}
