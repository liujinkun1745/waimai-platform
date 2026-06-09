package com.waimai.repository;

import com.waimai.entity.BalanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 余额记录数据访问层
 */
public interface BalanceRecordRepository extends JpaRepository<BalanceRecord, Long> {

    /** 按用户 ID 查找所有记录，按时间倒序 */
    List<BalanceRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}
