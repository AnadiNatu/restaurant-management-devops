package com.example.demo.adapter.web.service;

import com.example.demo.adapter.web.dto.CustomerDto;
import com.example.demo.domain.model.enums.CustomerType;

import java.util.List;

public interface CustomerService {
    CustomerDto.Response create(CustomerDto.CreateRequest request);
    CustomerDto.Response update(Long id, CustomerDto.UpdateRequest request);
    CustomerDto.Response getById(Long id);
    List<CustomerDto.Response> getAll();
    List<CustomerDto.Response> getByType(CustomerType type);
    void delete(Long id);
}
