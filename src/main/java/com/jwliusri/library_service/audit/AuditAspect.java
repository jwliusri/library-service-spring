package com.jwliusri.library_service.audit;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.jwliusri.library_service.article.ArticleResponse;
import com.jwliusri.library_service.security.LoginResponse;
import com.jwliusri.library_service.user.UserResponse;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void auditSuccessfulOperation(JoinPoint joinPoint, Auditable auditable, Object result) {
        createAuditLog(joinPoint, auditable, result, true, null);
    }

    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "ex")
    public void auditFailedOperation(JoinPoint joinPoint, Auditable auditable, Exception ex) {
        createAuditLog(joinPoint, auditable, null, false, ex.getMessage());
    }

    private void createAuditLog(JoinPoint joinPoint, Auditable auditable, Object result, boolean success, String error) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        String username = getCurrentUsername();
        String userAgent = request.getHeader("User-Agent");
        // String deviceInfo = userAgentParser.parse(userAgent).toString();
        String ipAddress = request.getRemoteAddr();

        AuditLog log = AuditLog.builder()
                .action(auditable.action())
                .entityType(auditable.entityType())
                .username(username)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .success(success)
                .errorMessage(error)
                .build();

        // Extract entity ID from method arguments or result
        extractEntityId(joinPoint, auditable, result).ifPresent(log::setEntityId);

        auditLogRepository.save(log);
    }

    private Optional<Long> extractEntityId(JoinPoint joinPoint, Auditable auditable, Object result) {
        // First try to get ID from result if it's a response DTO
        if (result != null && result instanceof ArticleResponse) {
            return Optional.of(((ArticleResponse) result).getId());
        }
        if (result != null && result instanceof UserResponse) {
            return Optional.of(((UserResponse) result).getId());
        }
        if (result != null && result instanceof LoginResponse) {
            return Optional.of(((LoginResponse) result).getId());
        }

        // Then try to get from method arguments
        return Arrays.stream(joinPoint.getArgs())
                .filter(arg -> arg instanceof Long || 
                             (arg != null && arg.getClass().isAnnotationPresent(AuditEntity.class)))
                .findFirst()
                .map(arg -> {
                    if (arg instanceof Long) {
                        return (Long) arg;
                    } else {
                        try {
                            return (Long) arg.getClass().getMethod("getId").invoke(arg);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "ANONYMOUS";
        }
        return authentication.getName();
    }
}
