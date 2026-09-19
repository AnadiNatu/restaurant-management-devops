package com.example.demo.adapter.web.service;

import com.example.demo.adapter.web.dto.OrderDto;
import com.example.demo.domain.model.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderDto.Response create(OrderDto.CreateRequest request, String handledBy);
    OrderDto.Response updateStatus(Long id, OrderDto.UpdateStatusRequest request, String updatedBy);
    OrderDto.Response getById(Long id);
    List<OrderDto.Response> getAll();
    List<OrderDto.Response> getByStatus(OrderStatus status);
    List<OrderDto.Response> getMyOrders(String handledBy);
    List<OrderDto.Response> getByCustomer(Long customerId);
    OrderDto.BillResponse generateBill(Long id, String actorEmail);
    void cancel(Long id, String actorEmail);

}
