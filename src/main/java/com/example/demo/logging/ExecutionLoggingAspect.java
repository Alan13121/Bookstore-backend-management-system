package com.example.demo.logging;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 自動記錄 Controller 與 Service 每個方法「執行到哪個階段」。
 *
 * <p>輸出範例（log 等級 DEBUG）：</p>
 * <pre>
 *   ▶ BookController.getBookById(99)
 *     ▶ BookService.getBookById(99)
 *     ✖ BookService.getBookById 丟出 ResourceNotFoundException: 找不到書籍，ID: 99 (3 ms)
 *   ✖ BookController.getBookById 丟出 ResourceNotFoundException: 找不到書籍，ID: 99 (4 ms)
 * </pre>
 *
 * <p><b>什麼是 AOP（剖面導向程式設計）</b>：把「每個方法都要做、但跟業務無關的事」（例如記 log、
 * 計時、交易控制）抽出來集中寫在一個地方，不用在每個方法裡重複寫。</p>
 * <ul>
 *   <li>{@code @Aspect}：這個類別是一個「切面」。</li>
 *   <li>{@code @Around(...)}：在符合條件的方法「前後」都插入邏輯，括號內的條件稱為切入點（Pointcut）。
 *       {@code within(com.example.demo.service..*)} 代表 service 套件（含子套件）裡所有類別的方法。</li>
 *   <li>{@code joinPoint.proceed()}：真正執行原本的方法；在它前後的程式就是插入的邏輯。</li>
 * </ul>
 *
 * <p>原理：Spring 會幫這些 Bean 產生「代理物件（Proxy）」，別人注入拿到的其實是代理，
 * 代理先執行切面，再呼叫真正的物件。{@code @Transactional} 也是用同樣的機制實作的。
 * 也因為如此，同一個類別裡 A 方法直接呼叫 B 方法時不會經過代理，B 就不會被記錄。</p>
 */
@Slf4j
@Aspect
@Component
public class ExecutionLoggingAspect {

    /** 每個參數在 log 中最多顯示幾個字。 */
    private static final int MAX_ARG_CHARS = 200;

    @Around("within(com.example.demo.controller..*) || within(com.example.demo.service..*)")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // 沒有開 DEBUG 時直接執行，省下組字串的成本
        if (!log.isDebugEnabled()) {
            return joinPoint.proceed();
        }

        String method = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();
        String indent = method.contains("Service.") ? "  " : ""; // Service 縮排一格，看得出呼叫層級
        log.debug("{}▶ {}({})", indent, method, formatArgs(joinPoint.getArgs()));

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.debug("{}◀ {} 完成 ({} ms)", indent, method, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable e) {
            // 只記錄後再往外丟，交給 GlobalExceptionHandler 處理，不改變原本的行為
            log.debug("{}✖ {} 丟出 {}: {} ({} ms)", indent, method,
                    e.getClass().getSimpleName(), e.getMessage(), System.currentTimeMillis() - start);
            throw e;
        }
    }

    /**
     * 把參數轉成字串。DTO 由 Lombok 產生 toString()，密碼欄位已用 {@code @ToString.Exclude} 排除；
     * Authentication 物件只顯示用戶名，避免印出整個登入者資料。
     */
    private String formatArgs(Object[] args) {
        return Arrays.stream(args)
                .map(this::formatArg)
                .collect(Collectors.joining(", "));
    }

    private String formatArg(Object arg) {
        if (arg instanceof Authentication authentication) {
            return "Authentication(" + authentication.getName() + ")";
        }
        String text = String.valueOf(arg);
        return text.length() > MAX_ARG_CHARS ? text.substring(0, MAX_ARG_CHARS) + "..." : text;
    }
}
