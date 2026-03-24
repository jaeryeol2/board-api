package com.project.api.entities;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "APP_USER")
public class AppUser {
  @Id
  private String userId;
  private String password;
  private String userName;
  private String userAddr;
  private String userRole;

  @QueryProjection
  public AppUser(String userId, String password) {
    this.userId = userId;
    this.password = password;
  }

}
