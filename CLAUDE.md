# CLAUDE.md

這是用來學習 Spring Boot 的個人測試專案（書店後台管理 API）。

## 開發流程

- 直接在 `main` 分支開發、commit 並 push，不需要另開分支或 PR。
- 提交前執行 `mvn spotless:apply` 和 `mvn test`，確認測試全數通過。
- 使用系統的 `mvn` 指令（`mvnw` 沒有執行權限）。

## 撰寫風格

- 使用者正在學習 Spring Boot：程式註解與說明使用繁體中文，著重解釋「為什麼這樣寫」與背後的 Spring 概念，不只描述程式在做什麼。
- 分層、命名、錯誤處理與狀態碼的慣例請參考 README.md 的「專案結構與開發慣例」。
- 新功能要附上測試（`src/test` 對應的套件下）。

## Log

- 每個請求的 log 由 `logging/RequestLoggingFilter`（request / response）與 `logging/ExecutionLoggingAspect`（Controller / Service 方法執行階段）產生，以 MDC 中的 requestId 串起來。
- 不可讓密碼、token 等敏感資料出現在 log：新增含密碼的 DTO 欄位要加 `@ToString.Exclude`；新的敏感 JSON 欄位要加進 `RequestLoggingFilter` 的遮蔽規則。

## 執行

- `docker-compose up --build -d` 啟動（MariaDB + App），`docker-compose logs -f springboot` 查看 log。
- 測試使用 H2 記憶體資料庫（`src/test/resources`）。
