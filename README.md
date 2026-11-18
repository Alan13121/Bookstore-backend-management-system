
---

# 📚 Bookshop Backend

一個基於 **Spring Boot + MariaDB + Docker** 的圖書商店後端服務，支援用戶認證、圖書管理、角色權限管理等功能。

---

## 🚀 功能特色

1. 使用 Spring Boot 架構
2. 集成 Spring Security + JWT 驗證
3. 使用 Spring Data JPA 操作 MariaDB
4. 啟動自動建表並初始化資料
5. 支援 Docker / Docker Compose 容器化部署
6. 提供 RESTful API

---

## 🛠️ 環境需求

* JDK 17+
* Maven 3.8+
* Docker
* Docker Compose

---

## ⚙️ 建置與啟動

### 1️⃣ 下載專案

```bash
git clone https://github.com/your-github-username/bookshop.git
cd bookshop
```

---

### 2️⃣ 確認配置

#### `src/main/resources/application.properties`

```properties
spring.application.name=demo

#指定資料庫密碼與diver
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

#在終端顯示hibernate產生的SQL指令
spring.jpa.show-sql=true

#用data、schema初始化資料庫
spring.sql.init.mode=always

#不靠JPA初始化
spring.jpa.hibernate.ddl-auto=none 

# JWT Token參數
jwt.secret=LyBGqP6T2B1q5m2jWj9KoP6x3F4Xk9R4p3WvZ5vL6Q8bY7R2e5S6c1G2x3N4t5Z6
jwt.expiration-ms=86400000

# 本地啟動
# spring.datasource.url=jdbc:mariadb://localhost:3300/bookshop

# docker啟動
spring.datasource.url=jdbc:mariadb://mariadb:3306/bookshop
```

---

## 🧪 初始化測試資料

系統啟動後，會自動執行以下檔案進行資料初始化：

* 資料表建構：`src/main/resources/schema.sql`
* 測試資料匯入：`src/main/resources/data.sql`

### 預設帳號資訊（密碼皆為 Bcrypt 加密，原始密碼為 `6969`）

| Username | Password |
| -------- | -------- |
| `admin`  | `6969`   |
| `staff`  | `6969`   |
| `worker` | `6969`   |

---

## 🐳 Docker 設定

### `docker-compose.yml`

```yaml
version: '3.8'

services:
  mariadb:
    image: mariadb:10.11
    container_name: bookshop_db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: bookshop
    ports:
      - "3306:3306"
    volumes:
      - mariadb_data:/var/lib/mysql

  springboot:
    build: .
    container_name: bookshop_app
    ports:
      - "8080:8080"
    depends_on:
      - mariadb
    restart: on-failure

volumes:
  mariadb_data:
```

---

### `Dockerfile`

```dockerfile
# --- Stage 1: Build the jar ---
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Run the app ---
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

---

### 3️⃣ 建立 Docker Image 並啟動

```bash
docker-compose up --build -d
```

---

### 4️⃣ 確認服務狀態

```bash
docker ps
```

查看應用日誌：

```bash
docker logs -f bookshop_app
```

成功訊息範例：

```
Started DemoApplication in X.XXX seconds
```

---

## 🌐 測試服務

開啟瀏覽器並輸入：

```
http://localhost:8080/
```
swagger 管理API：

```
http://localhost:8080/swagger-ui/index.html
```

---

## 🧱 專案結構與開發慣例

```
com.example.demo
├── controller   # 只負責接收請求、呼叫 Service、回傳 DTO，不寫商業邏輯
├── service      # 商業邏輯、交易控制（@Transactional）、Entity ↔ DTO 轉換
├── repository   # Spring Data JPA 介面
├── entity       # JPA Entity，不直接對外回傳
├── dto          # 請求 / 回應物件
├── exception    # 自訂例外與 GlobalExceptionHandler
├── security     # SecurityConfig、JWT、動態授權 Filter
└── config       # 其他設定（OpenAPI）
```

**命名**

| 類型 | 規則 | 範例 |
|---|---|---|
| 請求 DTO | `Xxx` + `CreateRequest` / `UpdateRequest` | `BookCreateRequest` |
| 回應 DTO | `Xxx` + `Response` | `BookResponse` |
| Controller / Service 方法 | `getAllXxx`、`getXxxById`、`createXxx`、`updateXxx`、`deleteXxx` | `getBookById` |
| Service 內的轉換方法 | `toResponse(entity)`（private） | |

**錯誤處理與狀態碼**

- Service 找不到資料時丟 `ResourceNotFoundException` → 404
- 資料重複（例如用戶名已存在）丟 `DuplicateResourceException` → 409
- 未登入 / 帳密錯誤 → 401；參數不合法（`IllegalArgumentException`）→ 400
- 錯誤回應格式一律為 `{ "status": 404, "message": "..." }`
- 刪除成功一律回 `204 No Content`

**格式**：提交前執行 `mvn spotless:apply`（4 空白縮排、import 依 java → jakarta → javax → org → com → io → lombok 排序，不使用萬用字元 import）。

---

## ⛔ 停止與清除

停止所有容器：

```bash
docker-compose down
```

刪除容器與資料卷：

```bash
docker-compose down -v
```

---
