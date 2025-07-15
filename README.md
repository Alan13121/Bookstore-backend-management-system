
---

# 📚 Bookshop Backend

一個基於 **Spring Boot + MariaDB + Docker** 的圖書商店後端服務，支持用戶認證、圖書管理、角色權限管理等功能。

---


## 🌟 功能特性

✅ 使用 Spring Boot 框架構建
✅ 集成 Spring Security 和 JWT
✅ 使用 Spring Data JPA 操作 MariaDB
✅ 自動建表、初始化資料
✅ 以 Docker / Docker Compose 容器化部署
✅ RESTful API

---

## 🚀 環境需求

* JDK 17+
* Maven 3.8+
* Docker
* Docker Compose

---

## 🔧 建置與啟動

### 1️⃣ 下載專案

```bash
git clone https://github.com/your-github-username/bookshop.git
cd bookshop
```

---

### 2️⃣ 編譯打包

```bash
./mvnw clean package -DskipTests
```

或（已安裝 Maven）

```bash
mvn clean package -DskipTests
```

打包完成後會在 `target/` 下產生：

```
target/demo-0.0.1-SNAPSHOT.jar
```

---

### 3️⃣ 確認配置

#### `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mariadb://mariadb:3306/bookshop
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

#### `src/main/resources/data.sql`

> 初始化測試資料

```sql
INSERT INTO users (id, username, password, full_name, email, enabled, created_at)
VALUES 
(1, 'admin', '{bcrypt}$2a$10$abcdefg1234567...', 'Administrator', 'admin@example.com', 1, NOW());

INSERT INTO roles (id, name)
VALUES
(1, 'ROLE_ADMIN');

INSERT INTO user_roles (user_id, role_id)
VALUES
(1, 1);
```

#### `docker-compose.yml`

```yaml
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

### 4️⃣ 建立 Docker Image 並啟動

```bash
docker-compose up --build -d
```

---

### 5️⃣ 確認服務狀態

查看容器狀態：

```bash
docker ps
```

查看日誌：

```bash
docker logs -f bookshop_app
```

看到：

```
Started DemoApplication in xx seconds
```

表示啟動成功。

---

## 🧪 測試服務

打開瀏覽器訪問：

```
http://localhost:8080/
```

或測試 API：

```bash
curl http://localhost:8080/api/your-endpoint
```

---

## 🗑️ 停止與清理

停止容器：

```bash
docker-compose down
```

刪除容器和資料卷：

```bash
docker-compose down -v
```
