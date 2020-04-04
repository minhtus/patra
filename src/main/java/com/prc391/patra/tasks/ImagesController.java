package com.prc391.patra.tasks;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.utils.S3Service;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
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
    private final TaskService taskService;
    private static final String IMAGES_CONTEXT_PATH = "/api/v0/images/";

    @PostMapping
    public void uploadImage(@RequestParam("file") MultipartFile file,
                            @RequestParam(value = "taskId") String taskId) throws IOException, EntityNotFoundException {
        // TODO check file type
        String resourceName = s3Service.upload(file);
        boolean result = taskService.attachImage(taskId, IMAGES_CONTEXT_PATH + resourceName);
        if (result) {
            ResponseEntity.ok().build();
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{resource}")
    public ResponseEntity<InputStreamResource> downloadImage(@PathVariable("resource") String resourceName) throws IOException, EntityNotFoundException {
        String filename = resourceName.substring(resourceName.lastIndexOf("_") + 1);
        Resource resource = s3Service.download(resourceName);
        if (!resource.exists()) throw new EntityNotFoundException("File not found");
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.IMAGE_PNG)
                .contentLength(resource.contentLength())
                .body(new InputStreamResource(resource.getInputStream()));
    }

}
