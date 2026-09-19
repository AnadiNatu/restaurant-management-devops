package com.example.demo.adapter.persistence;

import com.example.demo.adapter.persistence.mapper.CustomerMapper;
import com.example.demo.adapter.persistence.repository.CustomerRepository;
import com.example.demo.domain.model.Customer;
import com.example.demo.domain.model.enums.CustomerType;
import com.example.demo.domain.port.CustomerPort;
import com.example.demo.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomerPortAdapter implements CustomerPort {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Override
    public Customer save(Customer customer) {
        return mapper.toDomain(repository.save(mapper.toEntity(customer)));
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        return repository.findByPhone(phone).map(mapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Customer> findByType(CustomerType type) {
        return repository.findByCustomerType(type.name()).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Customer", "id", id);
        repository.deleteById(id);
    }
}