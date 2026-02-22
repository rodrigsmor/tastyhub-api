package com.rodrigo.tastyhub.shared.infrastructure.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.file.Files;
import java.nio.file.Path;

@Aspect
@Component
public class FileCleanupAspect {
    private final FileRollbackContext context;

    public FileCleanupAspect(FileRollbackContext context) {
        this.context = context;
    }

    @Before("@annotation(com.rodrigo.tastyhub.shared.kernel.annotations.FileCleanup)")
    public void registerCleanupHook() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                Path path = context.getForCleanup();

                if (status == TransactionSynchronization.STATUS_COMMITTED && path != null) {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception e) {
                        System.err.println("CRITICAL: Failed to cleanup file after commit: " + path);
                    }
                }
                context.clear();
                }
            });
        }
    }
}
