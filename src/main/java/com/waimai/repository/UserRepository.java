package com.waimai.repository;

import com.waimai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 用户数据访问层
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 按用户名查找 */
    Optional<User> findByUsername(String username);

    /** 按手机号查找 */
    Optional<User> findByPhone(String phone);

    /** 检查用户名是否存在 */
    boolean existsByUsername(String username);

    /** 检查手机号是否存在 */
    boolean existsByPhone(String phone);
}
