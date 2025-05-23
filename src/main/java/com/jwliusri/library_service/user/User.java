package com.jwliusri.library_service.user;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String fullName;
    private String username;
    private String email;
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoleEnum role = RoleEnum.ROLE_VIEWER;
    
    @ColumnDefault("true")
    @Builder.Default
    private boolean accountNonLocked = true;
    @ColumnDefault("0")
    @Builder.Default
    private int failedAttempt = 0;
    private LocalDateTime failedAttemptTime;
    private LocalDateTime lockTime;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
