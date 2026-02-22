package com.rodrigo.tastyhub.shared.config.storage;

import com.rodrigo.tastyhub.shared.kernel.annotations.FileCleanup;
import com.rodrigo.tastyhub.shared.infrastructure.aspect.FileRollbackContext;
import com.rodrigo.tastyhub.shared.kernel.annotations.FileRollback;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageStorageService {
    @Value("${upload.dir}")
    private String uploadDir;

    private final FileRollbackContext rollbackContext;

    public ImageStorageService(FileRollbackContext rollbackContext) {
        this.rollbackContext = rollbackContext;
    }

    @Transactional
    @FileRollback
    public String storeImage(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = file.getOriginalFilename();
            String extension = "";

            if (originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            rollbackContext.setForRollback(filePath);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store image. Error: " + e.getMessage());
        }
    }

    @Transactional
    @FileCleanup
    public void deleteImage(String fileName) {
        Path filePath = Paths.get(uploadDir).resolve(fileName).toAbsolutePath();

        if (Files.exists(filePath)) {
            rollbackContext.setForCleanup(filePath);
        }
    }
}
