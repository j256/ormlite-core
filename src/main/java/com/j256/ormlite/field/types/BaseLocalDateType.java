package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;

/**
 * Base class for all of the java.time class types.
 *
 * @author graynk
 */
public abstract class BaseLocalDateType extends BaseDataType {
    private static final String specificationVersion = System.getProperty("java.specification.version");
    private static final boolean javaTimeSupported = !(specificationVersion.equals("1.6") || specificationVersion.equals("1.7"));

    protected BaseLocalDateType(SqlType sqlType, Class<?>[] classes) {
        super(sqlType, classes);
    }

    protected BaseLocalDateType(SqlType sqlType) {
        super(sqlType);
    }

    protected static boolean isJavaTimeSupported() {
        return javaTimeSupported;
    }

    @Override
    public boolean isValidForVersion() {
        return true;
    }

    @Override
    public boolean isArgumentHolderRequired() {
        return true;
    }
}
