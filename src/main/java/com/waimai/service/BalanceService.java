package com.waimai.service;

import com.waimai.entity.BalanceRecord;
import com.waimai.entity.User;
import com.waimai.repository.BalanceRecordRepository;
import com.waimai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 余额服务
 */
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRecordRepository balanceRecordRepository;
    private final UserRepository userRepository;

    /** 查看余额 */
    public java.math.BigDecimal getBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getBalance();
    }

    /** 查看余额变动记录 */
    public List<BalanceRecord> listRecords(Long userId) {
        return balanceRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
