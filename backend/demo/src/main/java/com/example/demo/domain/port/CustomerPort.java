package com.example.demo.domain.port;

import com.example.demo.domain.model.Customer;
import com.example.demo.domain.model.enums.CustomerType;

import java.util.List;
import java.util.Optional;

public interface CustomerPort {
    Customer save(Customer customer);
    Optional<Customer> findById(Long id);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhone(String phone);
    List<Customer> findAll();
    List<Customer> findByType(CustomerType type);
    void deleteById(Long id);
}
