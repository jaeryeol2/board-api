package com.board.api.common.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.EntityPathBase;
import lombok.extern.slf4j.Slf4j;

/**
 * query dsl uitl
 */
@Slf4j
public class QueryDslUtil {
  private QueryDslUtil() {}

  /**
   * query dsl 동적 컬럼을 만들어주는 "Projections.fields(clazz, ...args)"를 제너레이트해주는 함수입니다.
   * 
   * @param <T1>
   * @param <T2>
   * @param clazz
   * @param entity
   * @param aliases
   * @return
   */
  public static <T1, T2> QBean<T1> generateFields(Class<T1> clazz, EntityPathBase<T2> entity,
      Expression<?>... aliases) {
    final List<Expression<?>> expressions = new ArrayList<>();

    try {
      final Field[] qFields = entity.getClass().getFields();

      if (aliases != null && aliases.length > 0) {
        expressions.addAll(Arrays.asList(aliases));
      }

      for (Field qField : qFields) {
        addExpressionIfFieldExists(clazz, qField, entity, expressions);
      }
    } catch (Exception e) {
      log.error("Failed to generate QueryDSL fields : ", e);
    }

    return Projections.fields(clazz, expressions.toArray(new Expression[0]));
  }

  /**
   * DTO 클래스(또는 그 부모 클래스)에 Q타입과 동일한 이름의 필드가 존재하는지 확인하고, 존재할 경우에만 expressions 리스트에 추가합니다.
   * 
   * @param <T1>
   * @param <T2>
   * @param clazz
   * @param qField
   * @param entity
   * @param expressions
   * @throws IllegalAccessException
   */
  private static <T1, T2> void addExpressionIfFieldExists(Class<T1> clazz, Field qField,
      EntityPathBase<T2> entity, List<Expression<?>> expressions) throws IllegalAccessException {

    // 상속 구조까지 전부 뒤져서 필드가 있는지 확인
    if (hasFieldInHierarchy(clazz, qField.getName())) {
      expressions.add((Expression<?>) qField.get(entity));
    } else {
      log.trace("Field not found in DTO hierarchy: {}", qField.getName());
    }
  }

  /**
   * 클래스 및 부모 클래스 계층 구조를 거슬러 올라가며 필드가 존재하는지 확인합니다.
   * 
   * @param clazz
   * @param fieldName
   * @return
   */
  private static boolean hasFieldInHierarchy(Class<?> clazz, String fieldName) {
    Class<?> currentClass = clazz;

    // Object.class에 도달할 때까지 (더 이상 부모가 없을 때까지) 반복
    while (currentClass != null && currentClass != Object.class) {
      try {
        currentClass.getDeclaredField(fieldName);
        return true; // 현재 클래스나 부모 클래스에서 필드를 찾은 경우
      } catch (NoSuchFieldException e) {
        // 못 찾았으면 부모 클래스로 이동하여 다시 탐색
        currentClass = currentClass.getSuperclass();
      }
    }

    return false; // 최상위(Object)까지 갔는데도 없으면 매핑할 필드가 없는 것
  }
}
