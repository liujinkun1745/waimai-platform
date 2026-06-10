# 美味外卖 — 项目文档

## 项目概述

仿美团外卖全栈平台，Spring Boot 3.2 + Thymeleaf + Bootstrap 5 + MySQL 8.0。

- **消费者端**：首页浏览 → 商家详情 → 加购 → 结算 → 订单追踪 → 评价
- **商家端**：订单管理 → 商品管理 → 评价回复 → 收益统计
- **8 商家 / 59 商品 / 6 消费者 / 测试数据自动初始化**

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| JDK | Java | 25 |
| 后端 | Spring Boot | 3.2.5 |
| 安全 | Spring Security | 6.x |
| ORM | JPA / Hibernate | 6.x |
| 数据库 | MySQL | 8.0 |
| 模板 | Thymeleaf | 3.1 |
| 前端 | Bootstrap 5 + Chart.js | 5.3 / 4.4 |
| 工具 | Lombok | 1.18.38 |

## 项目结构

```
外卖/
├── pom.xml
├── src/main/java/com/waimai/
│   ├── WaimaiApplication.java          # 启动入口
│   ├── config/
│   │   ├── DataInitializer.java        # 测试数据初始化（CommandLineRunner）
│   │   └── SecurityConfig.java         # Spring Security 配置
│   ├── controller/
│   │   ├── AuthController.java         # 登录/注册/角色路由
│   │   ├── ConsumerController.java     # 消费者接口
│   │   └── MerchantController.java     # 商家接口
│   ├── entity/                         # JPA 实体 (10个)
│   │   ├── User.java, Address.java, BalanceRecord.java
│   │   ├── Merchant.java, Category.java, Product.java
│   │   ├── Order.java, OrderItem.java
│   │   └── Review.java, Coupon.java
│   ├── repository/                     # Spring Data JPA (10个)
│   └── service/                        # 业务逻辑 (10个)
├── src/main/resources/
│   ├── application.yml                 # 数据库/JPA/Thymeleaf 配置
│   ├── static/
│   │   ├── css/style.css               # 全局样式（700+ 行，美团黄主题）
│   │   ├── js/main.js                  # Toast/Confirm/加载条/NavScroll
│   │   └── images/product/             # 52 张 SVG 商品图
│   └── templates/
│       ├── login.html / register.html
│       ├── fragments/
│       │   ├── layout.html             # 全局导航栏 + 脚本 + 加载条
│       │   ├── consumer-nav.html       # 消费者底部导航
│       │   └── merchant-nav.html       # 商家底部导航
│       ├── consumer/                   # 10 个消费者页面
│       └── merchant/                   # 8 个商家页面
```

## 关键约定

### Controller 模式
- 所有 Controller 使用 `@RequiredArgsConstructor` 构造器注入
- 商家端通过 `currentMerchant(UserDetails)` 私有方法获取当前商家
- 消费者端通过 `currentUser(UserDetails)` 获取当前用户
- POST 表单用 `RedirectAttributes` 传 Flash 消息
- REST API 用 `@ResponseBody` + `Map<String, Object>` 返回 JSON

### Thymeleaf 注意
- **`${...}` 只在 `th:` 属性中生效**，普通 HTML 属性不会处理（如 `onclick="${...}"` 不行，必须 `th:onclick="'...' + ${...} + '...'"`）
- **`th:attr` 中分号 `;` 是属性分隔符**，不能在值里出现。复杂 JS 表达式改用 `th:onclick` / `th:style`
- **SpEL 不支持 Java 转型语法** `(int)Math.round()`，需在 Controller 预计算后传 Map
- Thymeleaf 3.1 移除了 `#request` / `#session` 等对象，用 model 属性替代

### 数据库
- `ddl-auto: update` — JPA 自动建表，首次启动 DataInitializer 插入测试数据
- 数据存在则跳过初始化（`count() == 0` 判断）
- Product.category 是 `FetchType.LAZY`，模板里循环访问会 N+1 查询，用 `JOIN FETCH` 优化

### 前端
- `main.js` (v=3) — 公共逻辑：Toast 通知、Confirm 弹窗、NavScroll、Alert 自动关闭、页面加载条
- 购物车逻辑已从 main.js 移除，各商家详情页用 IIFE 自包含（key: `waimai_merchant_{id}`）
- 商家端底部导航隐藏原有 `#mainNav`，用自定义 top-bar + bottom-nav
- 商品管理页的 toggle 按钮用 AJAX 不跳页，edit/add/delete 操作需确认后跳转

### CSS 主题
- 主色 `--yellow: #FFD101`，辅色 `--yellow-dark: #FFB800`
- 销售卡片黄色顶边 `card-accent`，统计卡片 `stat-card`
- 动画：`fadeIn`、`slideUp`、`slideDown`、`pulse`、`bounceIn`、`shake`
- 骨架屏 `.skeleton`、空状态 `.empty-state`、加载条 `#pageLoadingBar`

## 启动

```bash
# 数据库
CREATE DATABASE waimai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 配置 application.yml 中数据库密码

# 运行
cd 外卖
mvn spring-boot:run
# 访问 http://localhost:8080
```

## 测试账号

所有密码：`123456`

| 用户名 | 角色 | 手机号 |
|--------|------|--------|
| consumer1 ~ consumer6 | 消费者 | 13800138000 ~ 13800138005 |
| merchant1 ~ merchant8 | 商家 | 13900139000 ~ 13900139007 |
