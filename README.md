# 🛵 美味外卖 — 仿美团外卖平台

Spring Boot + Thymeleaf + Bootstrap 5 全栈外卖平台，消费者下单、商家管理、评价系统、优惠券等完整业务流程。

## 📸 功能概览

### 🧑 消费者端
| 功能 | 说明 |
|------|------|
| 首页 | 轮播图 + 分类导航 + 商家列表（按销量/评分排序、关键词搜索） |
| 商家详情 | 三 Tab 切换（点菜/评价/商家）、分类侧边栏、购物车飞入动画 |
| 购物车 | localStorage 按商家隔离、加减数量、起送价校验 |
| 下单结算 | 收货地址管理、优惠券选择、余额支付、送达时间预估 |
| 订单跟踪 | 进度步骤条（待接单→待配送→配送中→已完成）、倒计时 |
| 评价系统 | 三维度评分（口味/包装/配送）、文字评价、查看商家回复 |
| 每日神券 | 三选一领取、过期倒计时 |
| 个人中心 | 余额、地址、邮箱/密码修改 |

### 🏪 商家端
| 功能 | 说明 |
|------|------|
| 订单管理 | 仪表盘统计卡（待接单/今日订单/今日收益/店铺评分）、状态筛选、接单/配送/完成 |
| 商品管理 | 分类折叠、AJAX 上下架、拖拽移分类、新增/编辑商品 |
| 评价管理 | 评分分布图、评价列表、回复顾客（同步至消费者端） |
| 收益统计 | 今日/本周/本月收益、7天/30天趋势图（Chart.js）、商品销量排行 |
| 店铺设置 | 营业状态切换、店铺信息编辑 |

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 25, Spring Boot 3.2.5, Spring Security 6.x, JPA/Hibernate |
| 前端 | Thymeleaf 3.1, Bootstrap 5.3, Chart.js 4.4, Bootstrap Icons |
| 数据库 | MySQL 8.0, HikariCP |
| 构建 | Maven 3.9+, Lombok 1.18.38 |
| 主题 | 美团黄色调 (#FFD101)、CSS 变量体系 |

## 📁 项目结构

```
外卖/
├── pom.xml
├── README.md
├── src/main/java/com/waimai/
│   ├── WaimaiApplication.java          # 启动入口
│   ├── config/
│   │   ├── DataInitializer.java        # 测试数据初始化（8商家 59商品 6消费者）
│   │   └── SecurityConfig.java         # Spring Security 配置
│   ├── controller/
│   │   ├── AuthController.java         # 登录/注册/角色路由
│   │   ├── ConsumerController.java     # 消费者接口（首页/商家/下单/评价/神券）
│   │   └── MerchantController.java     # 商家接口（订单/商品/评价/收益）
│   ├── entity/                         # JPA 实体
│   │   ├── User.java, Address.java
│   │   ├── Merchant.java, Category.java, Product.java
│   │   ├── Order.java, OrderItem.java
│   │   ├── Review.java, Coupon.java
│   ├── repository/                     # Spring Data JPA 仓库
│   └── service/                        # 业务逻辑层
├── src/main/resources/
│   ├── application.yml                 # 数据库/JPA/Thymeleaf 配置
│   ├── static/
│   │   ├── css/style.css               # 全局样式（700+ 行，CSS 变量 + 动画）
│   │   ├── js/main.js                  # 公共 JS（Toast/确认弹窗/加载条）
│   │   └── images/product/             # 52 张 SVG 商品图
│   └── templates/
│       ├── login.html / register.html
│       ├── fragments/
│       │   ├── layout.html             # 导航栏 + 脚本 + 加载条
│       │   ├── consumer-nav.html       # 消费者底部导航（4 Tab）
│       │   └── merchant-nav.html       # 商家底部导航（3 Tab）
│       ├── consumer/
│       │   ├── index.html              # 首页（轮播/分类/商家列表）
│       │   ├── merchant-detail.html    # 商家详情（核心购物页，1000+ 行）
│       │   ├── orders.html             # 订单列表（步骤条 + 筛选）
│       │   ├── order-detail.html       # 订单详情（倒计时 + 骑手卡片）
│       │   ├── coupons-page.html       # 神券中心（三选一 + 倒计时）
│       │   ├── my.html                 # 个人中心
│       │   ├── search.html, review.html, address.html, balance.html
│       └── merchant/
│           ├── orders.html             # 订单管理（仪表盘统计卡）
│           ├── products-all.html       # 商品+分类合并管理（折叠/拖拽/AJAX）
│           ├── reviews.html            # 评价管理（评分分布图 + 回复）
│           ├── earnings.html           # 收益统计（Chart.js 趋势图）
│           ├── my.html, categories.html, products.html, shop-edit.html
```

## 🚀 快速启动

### 环境要求
- JDK 17+
- MySQL 8.0
- Maven 3.9+

### 1. 创建数据库
```sql
CREATE DATABASE waimai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 修改配置
编辑 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/waimai?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

### 3. 启动
```bash
cd 外卖
mvn clean spring-boot:run
```

访问 **http://localhost:8080**，首次启动自动初始化测试数据。

## 🔑 测试账号

| 角色 | 用户名 | 手机号 | 密码 |
|------|--------|--------|------|
| 消费者 1-6 | consumer1 ~ consumer6 | 13800138000 ~ 13800138005 | 123456 |
| 商家 1-8 | merchant1 ~ merchant8 | 13900139000 ~ 13900139007 | 123456 |

### 8 家商家
| # | 店名 | 分类 | 商品数 |
|---|------|------|--------|
| 1 | 🍜 美味家常菜 | 美食 | 12 |
| 2 | 🍰 甜蜜时光烘焙 | 甜点饮品 | 7 |
| 3 | 🍎 鲜果多水果坊 | 生鲜果蔬 | 7 |
| 4 | 🍔 汉堡王 | 小吃快餐 | 7 |
| 5 | 🍲 蜀味香火锅 | 火锅烧烤 | 7 |
| 6 | ☕ 星巴克咖啡 | 咖啡奶茶 | 7 |
| 7 | 🏪 美宜佳便利店 | 超市便利 | 7 |
| 8 | 🍗 肯德基 | 小吃快餐 | 5 |

## 🎨 UI 特性

- 美团黄色调主题 (`#FFD101`)
- 完整 CSS 变量体系（颜色/阴影/圆角/过渡）
- 移动端适配（底部导航栏、响应式布局）
- 动画效果：卡片滑入、购物车飞入、加载条、骨架屏、脉冲按钮
- Toast 通知系统、自定义确认弹窗
- 订单状态步骤条 + 配送倒计时
- 售罄商品半透明遮罩
- 优惠券过期倒计时（实时更新）

## 📝 注意事项

1. **CSRF**：开发阶段已关闭 CSRF，生产环境需开启
2. **图片**：商品图片为 SVG 占位符，可替换为真实图片
3. **支付**：模拟余额支付，非真实支付网关
4. **JPA**：`ddl-auto: update` 首次运行后建议改为 `validate`
5. **购物车**：`localStorage` 存储，刷新/关闭不丢失，按商家隔离

## 📄 License

MIT — 仅供学习参考，请勿用于商业用途。
