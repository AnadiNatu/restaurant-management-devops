package com.example.demo.adapter.persistence.mapper;

import com.example.demo.adapter.persistence.entity.CustomerEntity;
import com.example.demo.domain.model.Customer;
import com.example.demo.domain.model.enums.CustomerType;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toDomain(CustomerEntity entity) {
        if (entity == null) return null;
        Customer c = new Customer();
        c.setId(entity.getId());
        c.setName(entity.getName());
        c.setEmail(entity.getEmail());
        c.setPhone(entity.getPhone());
        c.setCustomerType(CustomerType.valueOf(entity.getCustomerType()));
        c.setAddress(entity.getAddress());
        c.setGstNumber(entity.getGstNumber());
        c.setTotalVisits(entity.getTotalVisits());
        c.setCreatedAt(entity.getCreatedAt());
        c.setUpdatedAt(entity.getUpdatedAt());
        return c;
    }

    public CustomerEntity toEntity(Customer domain) {
        if (domain == null) return null;
        return CustomerEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .email(domain.getEmail())
                .phone(domain.getPhone())
                .customerType(domain.getCustomerType() != null
                        ? domain.getCustomerType().name() : CustomerType.WALK_IN.name())
                .address(domain.getAddress())
                .gstNumber(domain.getGstNumber())
                .totalVisits(domain.getTotalVisits())
                .build();
    }

}