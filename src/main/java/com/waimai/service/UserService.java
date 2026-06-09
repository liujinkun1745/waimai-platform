package com.waimai.service;

import com.waimai.entity.*;
import com.waimai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 用户服务 — 负责注册、登录、资料修改、余额管理
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final BalanceRecordRepository balanceRecordRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 消费者注册
     */
    @Transactional
    public User registerConsumer(String username, String phone, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("手机号已被注册");
        }
        User user = User.builder()
                .username(username)
                .phone(phone)
                .password(passwordEncoder.encode(password))
                .email(email)
                .role("ROLE_CONSUMER")
                .balance(BigDecimal.ZERO)
                .build();
        return userRepository.save(user);
    }

    /**
     * 商家注册
     */
    @Transactional
    public User registerMerchant(String username, String phone, String password,
                                  String shopName, String shopAddress, String businessLicense,
                                  String description) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("手机号已被注册");
        }
        // 创建登录用户
        User user = User.builder()
                .username(username)
                .phone(phone)
                .password(passwordEncoder.encode(password))
                .role("ROLE_MERCHANT")
                .balance(BigDecimal.ZERO)
                .build();
        user = userRepository.save(user);

        // 创建商家店铺
        Merchant merchant = Merchant.builder()
                .user(user)
                .shopName(shopName)
                .shopAddress(shopAddress)
                .businessLicense(businessLicense)
                .description(description)
                .build();
        merchantRepository.save(merchant);

        return user;
    }

    /** 按用户名查找 */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /** 按 ID 查找 */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /** 更新用户基本信息 */
    public void updateProfile(Long userId, String email) {
        User user = findById(userId);
        user.setEmail(email);
        userRepository.save(user);
    }

    /** 修改密码 */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /** 充值 */
    @Transactional
    public void recharge(Long userId, BigDecimal amount) {
        User user = findById(userId);
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        BalanceRecord record = BalanceRecord.builder()
                .user(user)
                .amount(amount)
                .type("充值")
                .description("余额充值")
                .build();
        balanceRecordRepository.save(record);
    }
}
