package com.example.demo.adapter.web.service.impl;

import com.example.demo.adapter.web.dto.CustomerDto;
import com.example.demo.adapter.web.service.CustomerService;
import com.example.demo.domain.model.Customer;
import com.example.demo.domain.model.enums.CustomerType;
import com.example.demo.domain.port.CustomerPort;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerPort customerPort;

    @Override
    @Transactional
    public CustomerDto.Response create(CustomerDto.CreateRequest request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            customerPort.findByEmail(request.getEmail()).ifPresent(c -> {
                throw new DuplicateResourceException("Customer", "email", request.getEmail());
            });
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCustomerType(request.getCustomerType() != null
                ? request.getCustomerType() : CustomerType.WALK_IN);
        customer.setAddress(request.getAddress());
        customer.setGstNumber(request.getGstNumber());
        customer.setTotalVisits(0);

        Customer saved = customerPort.save(customer);
        log.info("[CUSTOMER] Created | id={} | name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CustomerDto.Response update(Long id, CustomerDto.UpdateRequest request) {
        Customer customer = customerPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (request.getName() != null) customer.setName(request.getName());
        if (request.getEmail() != null) customer.setEmail(request.getEmail());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getCustomerType() != null) customer.setCustomerType(request.getCustomerType());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        if (request.getGstNumber() != null) customer.setGstNumber(request.getGstNumber());

        Customer saved = customerPort.save(customer);
        log.info("[CUSTOMER] Updated | id={}", id);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto.Response getById(Long id) {
        return customerPort.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto.Response> getAll() {
        return customerPort.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto.Response> getByType(CustomerType type) {
        return customerPort.findByType(type).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        customerPort.deleteById(id);
        log.info("[CUSTOMER] Deleted | id={}", id);
    }

    private CustomerDto.Response toResponse(Customer c) {
        CustomerDto.Response r = new CustomerDto.Response();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setEmail(c.getEmail());
        r.setPhone(c.getPhone());
        r.setCustomerType(c.getCustomerType());
        r.setAddress(c.getAddress());
        r.setGstNumber(c.getGstNumber());
        r.setTotalVisits(c.getTotalVisits());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}