package com.example.demo.adapter.web.dto;


import com.example.demo.domain.model.enums.CustomerType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

public class CustomerDto {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String name;
        @Email
        private String email;
        private String phone;
        private CustomerType customerType;
        private String address;
        private String gstNumber;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        @Email
        private String email;
        private String phone;
        private CustomerType customerType;
        private String address;
        private String gstNumber;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private CustomerType customerType;
        private String address;
        private String gstNumber;
        private Integer totalVisits;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
