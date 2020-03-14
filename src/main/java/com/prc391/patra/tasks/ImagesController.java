package com.prc391.patra.tasks;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImagesController {

    @PostMapping("/images")
    public void uploadImage(@RequestParam("file") MultipartFile file) {
        // do read file here
    }

}
