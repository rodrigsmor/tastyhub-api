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
            return joinPoint.proceed(); // Se não tem transação, segue vida normal
        }

        // 2. Registra o sincronismo
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                Path path = context.getFilePath();
                if (status != TransactionSynchronization.STATUS_COMMITTED && path != null) {
                    try {
                        Files.deleteIfExists(path);
                        System.out.println("DEBUG: Arquivo deletado com sucesso no Rollback: " + path);
                    } catch (IOException e) {
                        System.err.println("DEBUG: Falha ao deletar arquivo: " + e.getMessage());
                    }
                }
                context.clear();
            }
        });

        // 3. Executa o método (storeImage)
        return joinPoint.proceed();
    }
}
