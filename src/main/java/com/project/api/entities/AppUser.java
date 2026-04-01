package com.project.api.entities;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Entity
@Table(name = "APP_USER")
@ToString
@Builder
public class AppUser {
  @Id
  @Column(updatable = false)
  private String userId;
  private String password;
  private String userName;
  private String userAddr;
  private String userRole;
  private String email;
  private boolean isActive;
  @Column(updatable = false)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime lastLoginAt;



}
