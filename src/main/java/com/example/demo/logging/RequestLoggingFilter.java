package com.example.demo.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 記錄每個 API 請求的 request / response。
 *
 * <p>輸出範例：</p>
 * <pre>
 *   INFO  [a1b2c3d4] → POST /api/books
 *   DEBUG [a1b2c3d4]   request body : {"title":"Spring Boot in Action", ...}
 *   ...（中間是 Controller / Service / SQL 的 log，也都帶著同一個 [a1b2c3d4]）
 *   INFO  [a1b2c3d4] ← 200 POST /api/books (35 ms)
 *   DEBUG [a1b2c3d4]   response body: {"id":4,"title":"Spring Boot in Action", ...}
 * </pre>
 *
 * <p><b>requestId 與 MDC</b>：每個 request 會產生一組短 id 放進 MDC（Mapped Diagnostic Context）。
 * MDC 是 log 框架提供、綁在目前執行緒上的 Map，log 格式裡寫 {@code %X{requestId}} 就會自動印出來。
 * 同一個 request 的所有 log 都帶著同一個 id，多人同時操作時也能把屬於同一個 request 的 log 挑出來。
 * 這個 id 也會放在回應 Header {@code X-Request-Id}，前端回報錯誤時附上它，就能直接搜尋 log。</p>
 *
 * <p><b>為什麼要用 ContentCaching 包裝</b>：request / response 的 body 是串流（stream），只能讀一次。
 * 如果在這裡先讀掉，Controller 就讀不到了。ContentCachingRequestWrapper 會在 Controller 讀取時
 * 順便複製一份，ContentCachingResponseWrapper 則先把回應暫存起來，最後一定要呼叫
 * {@code copyBodyToResponse()} 把內容真正送回給前端，否則前端會收到空的回應。</p>
 *
 * <p><b>順序</b>：{@code @Order(HIGHEST_PRECEDENCE)} 讓它排在所有 Filter（包含 Spring Security）的最外層，
 * 所以連 DynamicAuthorizationFilter 回傳的 403 也會被記錄到。</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    /** 回應 Header 名稱；前端也可以自己帶這個 Header 指定 requestId。 */
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /** MDC 中的 key，log 格式用 {@code %X{requestId}} 取出。 */
    public static final String MDC_KEY = "requestId";

    /** body 最多快取多少 bytes，避免上傳大檔案時佔用太多記憶體。 */
    private static final int MAX_CACHED_BODY_BYTES = 10 * 1024;

    /** log 中 body 最多顯示幾個字，太長的會截斷。 */
    private static final int MAX_LOGGED_BODY_CHARS = 2000;

    /**
     * 前端自帶的 requestId 只接受英數字與 -，長度 1～64。
     * 直接把外部輸入寫進 log 有「log 注入」風險（例如塞入換行偽造一行 log），所以要先檢查格式。
     */
    private static final Pattern VALID_REQUEST_ID = Pattern.compile("[A-Za-z0-9-]{1,64}");

    /** 敏感欄位：JSON 中這些欄位的值會被換成 ***，避免密碼或 token 出現在 log 裡。 */
    private static final Pattern SENSITIVE_JSON_FIELD =
            Pattern.compile("(\"(?:password|newPassword|token)\"\\s*:\\s*\")[^\"]*(\")");

    /** 這些路徑是頁面、靜態檔案或文件，記錄它們只會讓 log 變吵，所以略過。 */
    private static final List<String> SKIP_PREFIXES = List.of(
            "/js/", "/css/", "/static/", "/swagger-ui", "/v3/api-docs", "/actuator", "/favicon.ico");

    private static final List<String> SKIP_SUFFIXES = List.of(".html", ".js", ".css", ".ico");

    /** 回傳 true 的 request 不會經過這個 Filter。 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return "/".equals(uri)
                || SKIP_PREFIXES.stream().anyMatch(uri::startsWith)
                || SKIP_SUFFIXES.stream().anyMatch(uri::endsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        MDC.put(MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        ContentCachingRequestWrapper cachedRequest =
                new ContentCachingRequestWrapper(request, MAX_CACHED_BODY_BYTES);
        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);

        String method = request.getMethod();
        String path = request.getQueryString() == null
                ? request.getRequestURI()
                : request.getRequestURI() + "?" + request.getQueryString();
        long start = System.currentTimeMillis();
        log.info("→ {} {}", method, path);

        boolean failed = false;
        try {
            // 交給後面的 Filter 與 Controller 處理（傳入包裝過的物件，body 才會被快取）
            filterChain.doFilter(cachedRequest, cachedResponse);
        } catch (IOException | ServletException | RuntimeException e) {
            // 正常情況下例外會被 GlobalExceptionHandler 處理掉，會跑到這裡代表沒人處理，伺服器會回 500
            failed = true;
            log.error("未處理的例外: {} {}", method, path, e);
            throw e;
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            int status = failed ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR : cachedResponse.getStatus();

            // body 要等 Controller 讀完、寫完之後才拿得到，所以放在 doFilter 之後
            String requestBody = toLoggableBody(cachedRequest.getContentAsByteArray(), cachedRequest.getContentType());
            if (!requestBody.isEmpty()) {
                log.debug("  request body : {}", requestBody);
            }
            log.info("← {} {} {} ({} ms)", status, method, path, elapsed);
            String responseBody = toLoggableBody(cachedResponse.getContentAsByteArray(), cachedResponse.getContentType());
            if (!responseBody.isEmpty()) {
                log.debug("  response body: {}", responseBody);
            }

            // 一定要呼叫：把暫存的回應內容真正寫回給前端
            cachedResponse.copyBodyToResponse();
            // 執行緒會被重複使用（Tomcat 執行緒池），用完要清掉，否則下一個 request 會拿到舊的 id
            MDC.remove(MDC_KEY);
        }
    }

    /** 前端有帶合法的 X-Request-Id 就沿用，否則產生一組 8 碼的新 id。 */
    private String resolveRequestId(HttpServletRequest request) {
        String fromHeader = request.getHeader(REQUEST_ID_HEADER);
        if (fromHeader != null && VALID_REQUEST_ID.matcher(fromHeader).matches()) {
            return fromHeader;
        }
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /** 把 body 轉成適合寫進 log 的字串：只處理文字格式、遮蔽敏感欄位、太長就截斷。 */
    static String toLoggableBody(byte[] content, String contentType) {
        if (content.length == 0) {
            return "";
        }
        if (!isTextContent(contentType)) {
            return "[" + contentType + ", " + content.length + " bytes]";
        }
        // 本專案的 JSON 一律是 UTF-8；用固定編碼可以避免中文變成亂碼
        String body = new String(content, StandardCharsets.UTF_8);
        body = SENSITIVE_JSON_FIELD.matcher(body).replaceAll("$1***$2");
        if (body.length() > MAX_LOGGED_BODY_CHARS) {
            body = body.substring(0, MAX_LOGGED_BODY_CHARS) + "...(共 " + body.length() + " 字，已截斷)";
        }
        return body;
    }

    private static boolean isTextContent(String contentType) {
        if (contentType == null) {
            return false;
        }
        String type = contentType.toLowerCase();
        return type.contains("json") || type.startsWith("text/") || type.contains("xml")
                || type.contains("x-www-form-urlencoded");
    }
}
