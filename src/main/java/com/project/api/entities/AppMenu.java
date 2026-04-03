package com.project.api.entities;

import java.time.LocalDateTime;
import com.project.api.constants.MenuType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Entity
@Table(name = "app_menu")
@ToString
@Builder
public class AppMenu {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long parentId;
  @Enumerated(EnumType.STRING)
  private MenuType menuType;
  private String boardCode;
  private String iconClass;
  private String menuName;
  private String href;
  private Integer sortOrder;
  private boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
