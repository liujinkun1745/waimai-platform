package com.waimai.service;

import com.waimai.entity.Address;
import com.waimai.entity.User;
import com.waimai.repository.AddressRepository;
import com.waimai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址服务 — 修复: builder().id() → getReferenceById()
 */
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    /** 查找用户所有地址 */
    public List<Address> listByConsumer(Long consumerId) {
        return addressRepository.findByConsumerId(consumerId);
    }

    /** 添加地址 — 使用 getReferenceById */
    @Transactional
    public Address add(Long consumerId, String receiverName, String receiverPhone,
                        String province, String city, String district,
                        String detailAddress, Boolean isDefault) {
        if (Boolean.TRUE.equals(isDefault)) {
            addressRepository.clearDefaultByConsumerId(consumerId);
        }
        Address address = Address.builder()
                .consumer(userRepository.getReferenceById(consumerId))
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .province(province)
                .city(city)
                .district(district)
                .detailAddress(detailAddress)
                .isDefault(isDefault != null && isDefault)
                .build();
        return addressRepository.save(address);
    }

    /** 编辑地址 */
    @Transactional
    public void update(Long addressId, Long consumerId, String receiverName,
                        String receiverPhone, String province, String city,
                        String district, String detailAddress, Boolean isDefault) {
        if (Boolean.TRUE.equals(isDefault)) {
            addressRepository.clearDefaultByConsumerId(consumerId);
        }
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        address.setReceiverName(receiverName);
        address.setReceiverPhone(receiverPhone);
        address.setProvince(province);
        address.setCity(city);
        address.setDistrict(district);
        address.setDetailAddress(detailAddress);
        address.setIsDefault(isDefault != null && isDefault);
        addressRepository.save(address);
    }

    /** 删除地址 */
    @Transactional
    public void delete(Long addressId) {
        addressRepository.deleteById(addressId);
    }

    /** 查找单个地址 */
    public Address findById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
    }
}
