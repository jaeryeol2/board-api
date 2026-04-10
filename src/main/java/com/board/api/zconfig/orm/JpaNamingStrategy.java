package com.board.api.zconfig.orm;

import java.util.Locale;
import java.util.Optional;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class JpaNamingStrategy implements PhysicalNamingStrategy {
  private Identifier apply(final Identifier name, final JdbcEnvironment jdbcEnvironment) {
    if (name == null) {
      return null;
    }
    final StringBuilder builder = new StringBuilder(name.getText().replace('.', '_'));
    int i = 1;

    while (i < builder.length() - 1) {
      if (isUnderscoreRequired(builder.charAt(i - 1), builder.charAt(i), builder.charAt(i + 1))) {
        builder.insert(i, '_');
        i += 2; // 삽입된 언더스코어와 현재 문자를 건너뜀
      } else {
        i++;
      }
    }

    return getIdentifier(builder.toString(), name.isQuoted(), jdbcEnvironment);
  }

  protected Identifier getIdentifier(String name, final boolean quoted,
      final JdbcEnvironment jdbcEnvironment) {
    if (isCaseInsensitive(jdbcEnvironment)) {
      name = name.toUpperCase(Locale.ROOT);
    }
    return new Identifier(name, quoted);
  }

  protected boolean isCaseInsensitive(JdbcEnvironment jdbcEnvironment) {
    return Optional.ofNullable(jdbcEnvironment).isPresent();
  }

  private boolean isUnderscoreRequired(final char before, final char current, final char after) {
    return Character.isLowerCase(before) && Character.isUpperCase(current)
        && Character.isLowerCase(after);
  }

  @Override
  public Identifier toPhysicalCatalogName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
    return apply(logicalName, jdbcEnvironment);
  }

  @Override
  public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
    return apply(logicalName, jdbcEnvironment);
  }

  @Override
  public Identifier toPhysicalSchemaName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
    return apply(logicalName, jdbcEnvironment);
  }

  @Override
  public Identifier toPhysicalSequenceName(Identifier logicalName,
      JdbcEnvironment jdbcEnvironment) {
    return apply(logicalName, jdbcEnvironment);
  }

  @Override
  public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
    return apply(logicalName, jdbcEnvironment);
  }

  @Override
  public Identifier toPhysicalTypeName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
    return PhysicalNamingStrategy.super.toPhysicalTypeName(logicalName, jdbcEnvironment);
  }
}
