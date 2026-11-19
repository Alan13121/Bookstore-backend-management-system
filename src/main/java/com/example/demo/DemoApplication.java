package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * 應用程式進入點。
 *
 * <p>{@code @SpringBootApplication} 是三個註解的組合：</p>
 * <ul>
 *   <li>{@code @Configuration}：這個類別可以定義 Bean。</li>
 *   <li>{@code @EnableAutoConfiguration}：依照 classpath 上有哪些套件自動設定，
 *       例如有 spring-boot-starter-web 就自動啟動內嵌的 Tomcat。</li>
 *   <li>{@code @ComponentScan}：掃描 {@code com.example.demo} 套件以下所有標了
 *       {@code @Component / @Service / @Repository / @RestController} 的類別並註冊成 Bean，
 *       所以其他類別都必須放在這個套件底下。</li>
 * </ul>
 *
 * <p>{@code @OpenAPIDefinition} 設定 Swagger 文件的標題，啟動後可在
 * {@code /swagger-ui/index.html} 查看並測試所有 API。</p>
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Demo 後台管理 API",
        version = "21.0",
        description = "這是後臺管理系統的 API 文件"))
public class DemoApplication {

    public static void main(String[] args) {
        // 建立 Spring 容器、建立所有 Bean、啟動內嵌 Web 伺服器
        SpringApplication.run(DemoApplication.class, args);
    }
}
