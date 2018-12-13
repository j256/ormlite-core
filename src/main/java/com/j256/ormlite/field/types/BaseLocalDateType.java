package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;

public abstract class BaseLocalDateType extends BaseDataType {
    public BaseLocalDateType(SqlType sqlType, Class<?>[] classes) {
        super(sqlType, classes);
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
