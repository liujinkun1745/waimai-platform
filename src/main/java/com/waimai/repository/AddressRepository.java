package com.waimai.repository;

import com.waimai.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 收货地址数据访问层
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

    /** 按消费者 ID 查找所有地址 */
    List<Address> findByConsumerId(Long consumerId);

    /** 查找默认地址 */
    Address findByConsumerIdAndIsDefaultTrue(Long consumerId);

    /** 清除该用户的所有默认地址 */
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.consumer.id = :consumerId")
    void clearDefaultByConsumerId(Long consumerId);
}
