package com.board.api.entities;


import java.time.OffsetDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@Table(name = "app_user")
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(unique = true, nullable = false)
  private String userId;
  private String password;
  private String userName;
  private String userAddr;
  private String userRole;
  private String email;
  private boolean isActive;
  @CreationTimestamp
  @Column(updatable = false)
  private OffsetDateTime createdAt;
  @UpdateTimestamp
  private OffsetDateTime updatedAt;
  private OffsetDateTime lastLoginAt;

  public void updateLastLoginAt(OffsetDateTime lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
  }
}
