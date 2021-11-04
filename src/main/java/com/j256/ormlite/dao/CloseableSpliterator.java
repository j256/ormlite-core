package com.j256.ormlite.dao;

import java.io.Closeable;
import java.util.Spliterator;

public interface CloseableSpliterator<T> extends Spliterator<T>, Closeable {

    /**
     * Close any underlying SQL statements but swallow any SQLExceptions.
     */
    void closeQuietly();

}
