package com.example.demo.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.example.demo.dto.LoginRequest;
import com.example.demo.entity.Book;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BookRepository;
import com.example.demo.service.BookService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class ExecutionLoggingAspectTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(ExecutionLoggingAspect.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Level originalLevel;

    private BookRepository bookRepository;
    private BookService proxiedBookService;

    @BeforeEach
    void setUp() {
        originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        appender.start();
        logger.addAppender(appender);

        // 和 Spring 執行時一樣：用代理物件包住真正的 Service，呼叫時會先經過切面
        bookRepository = mock(BookRepository.class);
        AspectJProxyFactory factory = new AspectJProxyFactory(new BookService(bookRepository));
        factory.setProxyTargetClass(true);
        factory.addAspect(new ExecutionLoggingAspect());
        proxiedBookService = factory.getProxy();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        logger.setLevel(originalLevel);
    }

    private List<String> logs() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    @Test
    void logsMethodStartAndFinish() {
        Book book = new Book();
        book.setId(1);
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        proxiedBookService.getBookById(1);

        List<String> logs = logs();
        assertEquals(2, logs.size());
        assertTrue(logs.get(0).contains("▶ BookService.getBookById(1)"));
        assertTrue(logs.get(1).contains("◀ BookService.getBookById 完成"));
    }

    @Test
    void logsExceptionAndRethrowsIt() {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> proxiedBookService.getBookById(99));

        assertTrue(logs().get(1).contains("✖ BookService.getBookById 丟出 ResourceNotFoundException: 找不到書籍，ID: 99"));
    }

    @Test
    void doesNothingExtraWhenDebugIsOff() {
        logger.setLevel(Level.INFO);
        when(bookRepository.findById(1)).thenReturn(Optional.of(new Book()));

        proxiedBookService.getBookById(1);

        assertTrue(logs().isEmpty());
    }

    @Test
    void passwordIsExcludedFromRequestToString() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("6969");

        assertFalse(request.toString().contains("6969"));
        assertTrue(request.toString().contains("admin"));
    }
}
