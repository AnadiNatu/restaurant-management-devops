package com.example.demo.adapter.web.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {

    public void uploadFile(MultipartFile file) throws IOException;
    public byte[] downloadFile(String key);

}
