package com.rodrigo.tastyhub.shared.infrastructure.aspect;

import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class FileRollbackContext {
    private static final ThreadLocal<Path> filePathThreadLocal = new ThreadLocal<>();

    public void setFilePath(Path filePath) {
        filePathThreadLocal.set(filePath);
    }

    public Path getFilePath() {
        return filePathThreadLocal.get();
    }

    public void clear() {
        filePathThreadLocal.remove();
    }
}
