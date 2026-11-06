package com.example.demo.adapter.persistence;

import com.example.demo.adapter.persistence.mapper.InventoryTransactionMapper;
import com.example.demo.adapter.persistence.repository.InventoryTransactionRepository;
import com.example.demo.domain.model.InventoryTransaction;
import com.example.demo.domain.model.enums.InventoryTransactionType;
import com.example.demo.domain.port.InventoryTransactionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InventoryTransactionPortAdapter implements InventoryTransactionPort {

    private final InventoryTransactionRepository repository;
    private final InventoryTransactionMapper mapper;

    @Override
    public InventoryTransaction save(InventoryTransaction transaction) {
        return mapper.toDomain(repository.save(mapper.toEntity(transaction)));
    }

    @Override
    public List<InventoryTransaction> findByInventoryItemId(Long id) {
        return repository.findByInventoryItemIdOrderByCreatedAtDesc(id)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal sumCostByTypeBetween(InventoryTransactionType type, LocalDateTime from, LocalDateTime to) {
        return repository.sumCostByTypeBetween(type, from, to);
    }
}