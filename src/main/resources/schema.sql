-- ==========================================
-- 美味外卖平台 — MySQL 初始化脚本
-- 版本: 1.0.0
-- 说明: 此脚本用于手动初始化数据库
-- 使用 JPA 自动建表时，此脚本可跳过
-- ==========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS waimai
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE waimai;

-- ==========================================
-- 表结构 (由 JPA 自动生成，此处仅作参考)
-- ==========================================

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL COMMENT 'ROLE_CONSUMER / ROLE_MERCHANT',
    balance DECIMAL(10,2) DEFAULT 0.00,
    created_at DATETIME,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 商家表
CREATE TABLE IF NOT EXISTS `merchant` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    shop_name VARCHAR(100) NOT NULL,
    shop_avatar VARCHAR(500),
    shop_address VARCHAR(255) NOT NULL,
    business_license VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    rating DECIMAL(2,1) DEFAULT 4.5,
    monthly_sales INT DEFAULT 0,
    delivery_fee DECIMAL(6,2) DEFAULT 3.00,
    min_order_amount DECIMAL(6,2) DEFAULT 15.00,
    business_hours VARCHAR(50) DEFAULT '09:00-22:00',
    status VARCHAR(10) DEFAULT '营业中',
    created_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 商品分类表
CREATE TABLE IF NOT EXISTS `category` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (merchant_id) REFERENCES `merchant`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    image VARCHAR(500),
    price DECIMAL(8,2) NOT NULL,
    stock INT DEFAULT 999,
    sales INT DEFAULT 0,
    description VARCHAR(500),
    status VARCHAR(5) DEFAULT '上架',
    created_at DATETIME,
    FOREIGN KEY (merchant_id) REFERENCES `merchant`(id),
    FOREIGN KEY (category_id) REFERENCES `category`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 收货地址表
CREATE TABLE IF NOT EXISTS `address` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consumer_id BIGINT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(20),
    city VARCHAR(20),
    district VARCHAR(20),
    detail_address VARCHAR(255) NOT NULL,
    is_default BIT DEFAULT 0,
    FOREIGN KEY (consumer_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(30) NOT NULL UNIQUE,
    consumer_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    address_snapshot VARCHAR(500),
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(10) NOT NULL COMMENT '待付款/待接单/待配送/配送中/已完成/已取消',
    created_at DATETIME,
    paid_at DATETIME,
    accepted_at DATETIME,
    delivered_at DATETIME,
    completed_at DATETIME,
    FOREIGN KEY (consumer_id) REFERENCES `user`(id),
    FOREIGN KEY (merchant_id) REFERENCES `merchant`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单详情表
CREATE TABLE IF NOT EXISTS `order_item` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(100) NOT NULL,
    product_image VARCHAR(500),
    price DECIMAL(8,2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 余额记录表
CREATE TABLE IF NOT EXISTS `balance_record` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    type VARCHAR(10) NOT NULL COMMENT '充值/消费',
    description VARCHAR(255),
    created_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
