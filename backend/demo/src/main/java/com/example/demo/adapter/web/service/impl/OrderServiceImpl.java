package com.example.demo.adapter.web.service.impl;


import com.example.demo.adapter.web.dto.OrderDto;
import com.example.demo.adapter.web.service.OrderService;
import com.example.demo.domain.model.MenuItem;
import com.example.demo.domain.model.Order;
import com.example.demo.domain.model.OrderItem;
import com.example.demo.domain.model.enums.OrderStatus;
import com.example.demo.domain.port.CustomerPort;
import com.example.demo.domain.port.MenuItemPort;
import com.example.demo.domain.port.OrderPort;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.notification_email.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderPort orderPort;
    private final MenuItemPort menuItemPort;
    private final CustomerPort customerPort;
    private final EmailService emailService;

    @Override
    @Transactional
    public OrderDto.Response create(OrderDto.CreateRequest request, String handledBy) {
        Order order = new Order();
        order.setHandledBy(handledBy);
        order.setTableNumber(request.getTableNumber());
        order.setNotes(request.getNotes());
        order.setStatus(OrderStatus.PENDING);

        // Resolve customer
        if (request.getCustomerId() != null) {
            customerPort.findById(request.getCustomerId()).ifPresent(c -> {
                order.setCustomerId(c.getId());
                order.setCustomerName(c.getName());
                order.setCustomerEmail(c.getEmail());
                // Increment visit count
                c.setTotalVisits(c.getTotalVisits() + 1);
                customerPort.save(c);
            });
        } else {
            order.setCustomerName(request.getCustomerName());
            order.setCustomerEmail(request.getCustomerEmail());
        }

        // Build order items
        List<OrderItem> items = request.getItems().stream().map(req -> {
            MenuItem menuItem = menuItemPort.findById(req.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", req.getMenuItemId()));

            if (!Boolean.TRUE.equals(menuItem.getIsAvailable())) {
                throw new InvalidOperationException("createOrder",
                        "Menu item '" + menuItem.getName() + "' is not available");
            }

            BigDecimal taxRate = menuItem.getTaxRate() != null ? menuItem.getTaxRate() : BigDecimal.ZERO;
            BigDecimal unitPrice = menuItem.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(req.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            OrderItem item = new OrderItem();
            item.setMenuItemId(menuItem.getId());
            item.setMenuItemName(menuItem.getName());
            item.setQuantity(req.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setTaxRate(taxRate);
            item.setSubtotal(subtotal);

            return item;
        }).collect(Collectors.toList());

        order.setItems(items);

        BigDecimal subtotal = items.stream().map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount = items.stream().map(i ->
                        i.getSubtotal().multiply(i.getTaxRate())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(taxAmount).subtract(discount).setScale(2, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setDiscountAmount(discount);
        order.setTotalAmount(total);

        Order saved = orderPort.save(order);
        log.info("[ORDER] Created | id={} | total={} | by={}", saved.getId(), saved.getTotalAmount(), handledBy);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public OrderDto.Response updateStatus(Long id, OrderDto.UpdateStatusRequest request, String updatedBy) {
        Order order = orderPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOperationException("updateStatus", "Cannot update a cancelled order");
        }

        order.setStatus(request.getStatus());
        if (request.getNotes() != null) order.setNotes(request.getNotes());

        Order saved = orderPort.save(order);
        log.info("[ORDER] Status updated | id={} | status={} | by={}", id, request.getStatus(), updatedBy);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto.Response getById(Long id) {
        return orderPort.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto.Response> getAll() {
        return orderPort.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto.Response> getByStatus(OrderStatus status) {
        return orderPort.findByStatus(status).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto.Response> getMyOrders(String handledBy) {
        return orderPort.findByHandledBy(handledBy).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto.Response> getByCustomer(Long customerId) {
        return orderPort.findByCustomerId(customerId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDto.BillResponse generateBill(Long id, String actorEmail) {
        Order order = orderPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        order.setStatus(OrderStatus.BILLED);
        orderPort.save(order);

        String invoiceNumber = "INV-" + id + "-" + System.currentTimeMillis();

        // Email invoice if customer email present
        if (order.getCustomerEmail() != null && !order.getCustomerEmail().isBlank()) {
            try {
                emailService.sendHtmlEmail(order.getCustomerEmail(),
                        "Your Invoice - " + invoiceNumber,
                        buildInvoiceHtml(order, invoiceNumber));
                log.info("[ORDER] Invoice emailed | orderId={} | to={}", id, order.getCustomerEmail());
            } catch (Exception ex) {
                log.warn("[ORDER] Invoice email failed | orderId={} | error={}", id, ex.getMessage());
            }
        }

        return toBillResponse(order, invoiceNumber);
    }

    @Override
    @Transactional
    public void cancel(Long id, String actorEmail) {
        Order order = orderPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (order.getStatus() == OrderStatus.BILLED) {
            throw new InvalidOperationException("cancelOrder", "Cannot cancel a billed order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderPort.save(order);
        log.info("[ORDER] Cancelled | id={} | by={}", id, actorEmail);
    }

    // ── Response mappers ─────────────────────────────────────────────────────

    private OrderDto.Response toResponse(Order order) {
        OrderDto.Response r = new OrderDto.Response();
        r.setId(order.getId());
        r.setCustomerId(order.getCustomerId());
        r.setCustomerName(order.getCustomerName());
        r.setCustomerEmail(order.getCustomerEmail());
        r.setTableNumber(order.getTableNumber());
        r.setStatus(order.getStatus());
        r.setSubtotal(order.getSubtotal());
        r.setTaxAmount(order.getTaxAmount());
        r.setDiscountAmount(order.getDiscountAmount());
        r.setTotalAmount(order.getTotalAmount());
        r.setNotes(order.getNotes());
        r.setHandledBy(order.getHandledBy());
        r.setCreatedAt(order.getCreatedAt());
        r.setUpdatedAt(order.getUpdatedAt());
        if (order.getItems() != null) {
            r.setItems(order.getItems().stream().map(this::toItemResponse).collect(Collectors.toList()));
        }
        return r;
    }

    private OrderDto.OrderItemResponse toItemResponse(OrderItem item) {
        OrderDto.OrderItemResponse r = new OrderDto.OrderItemResponse();
        r.setId(item.getId());
        r.setMenuItemId(item.getMenuItemId());
        r.setMenuItemName(item.getMenuItemName());
        r.setQuantity(item.getQuantity());
        r.setUnitPrice(item.getUnitPrice());
        r.setTaxRate(item.getTaxRate());
        r.setSubtotal(item.getSubtotal());
        return r;
    }

    private OrderDto.BillResponse toBillResponse(Order order, String invoiceNumber) {
        OrderDto.BillResponse r = new OrderDto.BillResponse();
        r.setOrderId(order.getId());
        r.setCustomerName(order.getCustomerName());
        r.setCustomerEmail(order.getCustomerEmail());
        r.setTableNumber(order.getTableNumber());
        r.setSubtotal(order.getSubtotal());
        r.setTaxAmount(order.getTaxAmount());
        r.setDiscountAmount(order.getDiscountAmount());
        r.setTotalAmount(order.getTotalAmount());
        r.setHandledBy(order.getHandledBy());
        r.setBilledAt(LocalDateTime.now());
        r.setInvoiceNumber(invoiceNumber);
        if (order.getItems() != null) {
            r.setItems(order.getItems().stream().map(this::toItemResponse).collect(Collectors.toList()));
        }
        return r;
    }

    private String buildInvoiceHtml(Order order, String invoiceNumber) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:24px'>")
                .append("<h2 style='color:#e53935'>Tax Invoice</h2>")
                .append("<p><strong>Invoice #:</strong> ").append(invoiceNumber).append("</p>")
                .append("<p><strong>Date:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))).append("</p>")
                .append("<p><strong>Customer:</strong> ").append(order.getCustomerName()).append("</p>")
                .append("<p><strong>Table:</strong> ").append(order.getTableNumber()).append("</p>")
                .append("<table border='1' cellpadding='8' cellspacing='0' style='width:100%;border-collapse:collapse'>")
                .append("<tr style='background:#f5f5f5'><th>Item</th><th>Qty</th><th>Unit Price</th><th>Tax%</th><th>Subtotal</th></tr>");

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                sb.append("<tr>")
                        .append("<td>").append(item.getMenuItemName()).append("</td>")
                        .append("<td>").append(item.getQuantity()).append("</td>")
                        .append("<td>₹").append(item.getUnitPrice()).append("</td>")
                        .append("<td>").append(item.getTaxRate()).append("%</td>")
                        .append("<td>₹").append(item.getSubtotal()).append("</td>")
                        .append("</tr>");
            }
        }

        sb.append("</table>")
                .append("<p style='text-align:right'><strong>Subtotal: ₹").append(order.getSubtotal()).append("</strong></p>")
                .append("<p style='text-align:right'><strong>Tax: ₹").append(order.getTaxAmount()).append("</strong></p>")
                .append("<p style='text-align:right'><strong>Discount: ₹").append(order.getDiscountAmount()).append("</strong></p>")
                .append("<p style='text-align:right;font-size:18px;color:#e53935'><strong>Total: ₹").append(order.getTotalAmount()).append("</strong></p>")
                .append("<p style='margin-top:32px;color:#888;font-size:12px'>Thank you for dining with us!</p>")
                .append("</div>");
        return sb.toString();
    }
}
