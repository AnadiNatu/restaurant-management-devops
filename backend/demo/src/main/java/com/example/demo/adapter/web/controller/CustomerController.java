package com.example.demo.adapter.web.controller;

import com.example.demo.adapter.security.RestaurantUserDetails;
import com.example.demo.adapter.web.dto.CustomerDto;
import com.example.demo.adapter.web.service.ActivityLogService;
import com.example.demo.adapter.web.service.CustomerService;
import com.example.demo.domain.model.enums.CustomerType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final ActivityLogService activityLogService;

    @GetMapping("/getAll")
    public ResponseEntity<List<CustomerDto.Response>> getAll() {
        return ResponseEntity.ok(customerService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<CustomerDto.Response>> getByType(@PathVariable CustomerType type) {
        return ResponseEntity.ok(customerService.getByType(type));
    }
//http://localhost:8080/api/customers/create
//    timestamp: '2026-09-16T12:40:20.608563300', status: 500, error: 'Internal Server Error', message: 'could not execute statement [ERROR: duplicate key …(?,?,?,?,?,?,?,?,?)]; constraint [customers_pkey]', path: '/api/customers/create'}
//error
//:
//"Internal Server Error"
//message
//:
//"could not execute statement [ERROR: duplicate key value violates unique constraint \"customers_pkey\"\n  Detail: Key (id)=(3) already exists.] [insert into customers (address,created_at,customer_type,email,gst_number,name,phone,total_visits,updated_at) values (?,?,?,?,?,?,?,?,?)]; SQL [insert into customers (address,created_at,customer_type,email,gst_number,name,phone,total_visits,updated_at) values (?,?,?,?,?,?,?,?,?)]; constraint [customers_pkey]"
//path
//:
//"/api/customers/create"
//status
//:
//500
//timestamp
//:
//"2026-09-16T12:40:20.608563300"
//[[Prototype]]
//:
//Object
    @PostMapping("/create")
    public ResponseEntity<CustomerDto.Response> create(@Valid @RequestBody CustomerDto.CreateRequest request,
            @AuthenticationPrincipal RestaurantUserDetails principal, HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        CustomerDto.Response response = customerService.create(request);
        activityLogService.log(actor, principal.getUser().getRole(),
                "CREATE_CUSTOMER", "CUSTOMER", String.valueOf(response.getId()),
                "Created customer: " + response.getName(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDto.UpdateRequest request,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        CustomerDto.Response response = customerService.update(id, request);
        activityLogService.log(actor, principal.getUser().getRole(),
                "UPDATE_CUSTOMER", "CUSTOMER", String.valueOf(id),
                "Updated customer id: " + id, httpRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        customerService.delete(id);
        activityLogService.log(actor, principal.getUser().getRole(),
                "DELETE_CUSTOMER", "CUSTOMER", String.valueOf(id),
                "Deleted customer id: " + id, httpRequest);
        return ResponseEntity.noContent().build();
    }

}