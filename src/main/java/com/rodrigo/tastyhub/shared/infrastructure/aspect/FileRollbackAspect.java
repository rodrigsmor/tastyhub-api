package com.rodrigo.tastyhub.shared.infrastructure.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Aspect
@Component
public class FileRollbackAspect {

    private final FileRollbackContext context;

    public FileRollbackAspect(FileRollbackContext context) {
        this.context = context;
    }

    @Around("@annotation(com.rodrigo.tastyhub.shared.kernel.annotations.FileRollback)")
    public Object manageFileRollback(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return joinPoint.proceed();
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
            Path path = context.getForRollback();
            if (status != TransactionSynchronization.STATUS_COMMITTED && path != null) {
                try {
                    Files.deleteIfExists(path);
                    System.out.println("DEBUG: File deleted successfully: " + path);
                } catch (IOException e) {
                    System.err.println("DEBUG: Failed to delete file: " + e.getMessage());
                }
            }
            context.clear();
            }
        });

        return joinPoint.proceed();
    }
}
