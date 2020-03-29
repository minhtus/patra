package com.prc391.patra.tasks;

import com.prc391.patra.utils.S3Service;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/v0/images")
@AllArgsConstructor
public class ImagesController {
    private final S3Service s3Service;

    @PostMapping
    public void uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        //TODO write db and hold file path
        s3Service.upload(file);
    }

    @GetMapping("/{url}")
    public ResponseEntity<InputStreamResource> downloadImage(@PathVariable("url") String resourceUrl) throws IOException {
        //TODO get filename and authorize
        String filename = "//TODO";
        InputStreamResource inputStreamResource = new InputStreamResource(s3Service.download(resourceUrl));
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=" + filename)
                .contentType(MediaType.IMAGE_PNG)
                .body(inputStreamResource);
    }

}
