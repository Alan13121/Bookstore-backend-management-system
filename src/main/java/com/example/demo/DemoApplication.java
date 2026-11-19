package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * 應用程式進入點。
 *
 * <p>{@code @SpringBootApplication} 其實是三個註解的組合：
 * <ul>
 *   <li>{@code @Configuration}：這個類別本身可以定義 Bean。</li>
 *   <li>{@code @EnableAutoConfiguration}：依照 classpath 上的套件自動設定
 *       （例如有 spring-boot-starter-web 就自動啟動內嵌 Tomcat）。</li>
 *   <li>{@code @ComponentScan}：掃描本套件 {@code com.example.demo} 以下所有
 *       {@code @Component / @Service / @Repository / @RestController} 並註冊成 Bean。
 *       所以其他類別都必須放在這個套件底下才會被掃到。</li>
 * </ul>
 *
 * <p>{@code @OpenAPIDefinition} 設定 Swagger 文件標題，啟動後可在
 * {@code /swagger-ui/index.html} 查看所有 API。</p>
 */
@OpenAPIDefinition(
    info = @Info(
            title = "Demo 後台管理 API",
            version = "21.0",
            description = "這是後臺管理糸統的 API 文件"
    )
)

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        // 建立 Spring 容器（ApplicationContext）、建立所有 Bean、啟動內嵌 Web 伺服器
        SpringApplication.run(DemoApplication.class, args);
    }

}
