package com.project.api.entities;

import java.time.LocalDateTime;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@Table(name = "APP_USER")
@ToString
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

  @QueryProjection
  public AppUser(String userId, String password) {
    this.userId = userId;
    this.password = password;
  }

  @QueryProjection
  public AppUser(String userId, String userName, String userAddr, String email) {
    this.userId = userId;
    this.userName = userName;
    this.userAddr = userAddr;
    this.email = email;
  }

}
