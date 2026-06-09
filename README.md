# 美味外卖 — 仿美团外卖本地生活服务平台

一个使用 **Java Spring Boot 3.x + MySQL 8.0 + Bootstrap 5 + Thymeleaf** 构建的完整外卖平台系统。

## 功能概览

### 消费者端
- 🔍 商家浏览、搜索、排序（按销量/评分）
- 🛒 购物车（右侧悬浮，实时计算总价）
- 📦 下单流程（选择地址 → 余额支付 → 提交订单）
- 📋 订单管理（待接单/待配送/配送中/已完成/已取消）
- 📍 收货地址管理（CRUD + 默认地址）
- 💰 余额系统（充值 + 消费记录）
- 👤 个人中心（信息修改 + 密码修改）

### 商家端
- 📊 仪表盘（待处理订单、今日订单数、商品数）
- 🏪 店铺管理（信息编辑 + 营业状态切换）
- 🏷️ 商品分类管理（增删改）
- 📦 商品管理（增删改 + 上下架）
- 📋 订单管理（接单 → 配送 → 完成）
- 📈 收益统计（今日/昨日/本周/本月 + Chart.js 趋势图）

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5 |
| 安全框架 | Spring Security 6.x |
| ORM | Spring Data JPA (Hibernate) |
| 数据库 | MySQL 8.0 |
| 模板引擎 | Thymeleaf |
| 前端框架 | Bootstrap 5.3 |
| 图表库 | Chart.js 4.4 |
| 构建工具 | Maven 3.9+ |
| Java 版本 | 17+ |

## 项目结构

```
外卖/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/waimai/
    │   ├── WaimaiApplication.java          # 主启动类
    │   ├── config/
    │   │   ├── SecurityConfig.java          # Spring Security 配置
    │   │   ├── MvcConfig.java               # MVC 视图控制器
    │   │   └── DataInitializer.java         # 测试数据初始化
    │   ├── entity/                           # JPA 实体类 (8个)
    │   ├── repository/                       # JPA Repository (8个)
    │   ├── service/                          # 业务服务层 (8个)
    │   ├── controller/                       # 控制器 (4个)
    │   └── exception/
    │       └── GlobalExceptionHandler.java
    └── resources/
        ├── application.yml                   # 应用配置
        ├── schema.sql                        # 建表参考 SQL
        ├── static/css/style.css              # 全局样式 (美团黄)
        ├── static/js/main.js                 # 购物车/分类切换
        └── templates/                        # Thymeleaf 模板 (17个)
```

## 运行步骤

### 1. 环境要求

- **JDK 17** 或更高
- **MySQL 8.0** 或更高
- **Maven 3.9** 或更高（或使用 IDE 内置 Maven）

### 2. 数据库配置

创建 MySQL 数据库（编码 utf8mb4）：

```sql
CREATE DATABASE IF NOT EXISTS waimai
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/waimai?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root        # 改为你的 MySQL 用户名
    password: root        # 改为你的 MySQL 密码
```

### 3. 启动项目

```bash
# 方法一：Maven 命令行
cd 外卖
mvn clean spring-boot:run

# 方法二：打包运行
mvn clean package -DskipTests
java -jar target/waimai-platform-1.0.0.jar
```

### 4. 访问系统

浏览器打开：**http://localhost:8080**

首次启动时，系统会自动创建数据库表并插入测试数据。

## 测试账号

| 角色 | 手机号 | 密码 | 备注 |
|------|--------|------|------|
| 消费者 | 13800138000 | 123456 | 余额 ¥1000 |
| 商家1 | 13900139000 | 123456 | 美味家常菜 (美食) |
| 商家2 | 13900139001 | 123456 | 甜蜜时光烘焙 (甜点) |
| 商家3 | 13900139002 | 123456 | 鲜果鲜生 (水果) |

> 所有商家密码均为 `123456`，也可使用用户名登录：`consumer1`、`merchant1`、`merchant2`、`merchant3`

## 配置说明

### application.yml 关键配置

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update    # 首次运行后建议改为 validate
    show-sql: false       # 调试时可改为 true 查看 SQL
server:
  port: 8080
```

`DataInitializer.java` 在首次启动且用户表为空时自动插入测试数据，重复启动不会重复插入。

## 注意事项

1. **CSRF**：开发阶段已在 `SecurityConfig` 中关闭 CSRF 防护，生产环境应开启
2. **图片**：商品图片使用 SVG 占位符，可替换为真实图片 URL
3. **支付**：当前为模拟余额支付，非真实支付网关
4. **JPA ddl-auto**：首次运行后建议将 `ddl-auto` 从 `update` 改为 `validate`
5. **购物车**：使用浏览器 `localStorage` 存储，跨页面不丢失

## License

MIT License — 仅供学习交流使用
