package com.j256.ormlite.field.types;

import java.sql.SQLException;
import java.util.Currency;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Type that persists a {@link Currency} object.
 * 
 * @author Ian Kirk
 */
public class CurrencyPersister extends BaseDataType {

  public static int DEFAULT_WIDTH = 3;

  private static final CurrencyPersister singleTon = new CurrencyPersister();

  public static CurrencyPersister getSingleton() {
    return singleTon;
  }

  public CurrencyPersister() {
    super(SqlType.STRING);
  }

  public CurrencyPersister(final SqlType sqlType, final Class<?>[] classes) {
    super(sqlType, classes);
  }

  @Override
  public int getDefaultWidth() {
    return DEFAULT_WIDTH;
  }

  @Override
  public Object javaToSqlArg(
    final FieldType fieldType,
    final Object javaObject
  ) throws SQLException {
    final Currency currency = (Currency) javaObject;
    return currency.getCurrencyCode();
  }

  @Override
  public Object parseDefaultString(final FieldType fieldType, final String defaultStr) throws SQLException {
    try {
    return Currency.getInstance(defaultStr);
    } catch (NullPointerException|IllegalArgumentException e) {
      throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default Country string '"
        , e);
    }
  }

  @Override
  public Object resultToSqlArg(final FieldType fieldType, final DatabaseResults results, final int columnPos) throws SQLException {
    return results.getString(columnPos);
  }

  @Override
  public Object sqlArgToJava(final FieldType fieldType, final Object sqlArg, final int columnPos) throws SQLException {
    final String currencyStr = (String) sqlArg;

    try {
      return Currency.getInstance(currencyStr);
    } catch (NullPointerException | IllegalArgumentException e) {
      throw SqlExceptionUtil.create("Problems with column " + columnPos + " parsing Currency-string '" + currencyStr 
        + "'", e);
    }
  }
}
