package com.rodrigo.tastyhub.shared.infrastructure.aspect;

import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class FileRollbackContext {
    private final ThreadLocal<Path> fileToRollback = new ThreadLocal<>();
    private final ThreadLocal<Path> fileToCleanup = new ThreadLocal<>();

    public void setForRollback(Path path) { fileToRollback.set(path); }
    public void setForCleanup(Path path) { fileToCleanup.set(path); }

    public Path getForRollback() { return fileToRollback.get(); }
    public Path getForCleanup() { return fileToCleanup.get(); }

    public void clear() {
        fileToRollback.remove();
        fileToCleanup.remove();
    }
}
