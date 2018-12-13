package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;

public abstract class BaseLocalDateType extends BaseDataType {
    protected BaseLocalDateType(SqlType sqlType, Class<?>[] classes) {
        super(sqlType, classes);
    }

    protected BaseLocalDateType(SqlType sqlType) {
        super(sqlType);
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
