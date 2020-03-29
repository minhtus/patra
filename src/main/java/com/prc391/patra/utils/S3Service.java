package com.prc391.patra.utils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class S3Service {
    private final ResourceLoader resourceLoader;
    private final String S3_IMAGES_CONTEXT_URL = "s3://patra/images";

    public S3Service(@Qualifier("webApplicationContext") ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void upload(MultipartFile file) throws IOException {
        WritableResource writableResource = (WritableResource) resourceLoader
                .getResource(String.format("%s/%s_%s", S3_IMAGES_CONTEXT_URL, System.currentTimeMillis(), file.getOriginalFilename()));
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = writableResource.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) > -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    public InputStream download(String resource) throws IOException {
        return resourceLoader.getResource(String.format("%s/%s", S3_IMAGES_CONTEXT_URL, resource)).getInputStream();
    }
}
