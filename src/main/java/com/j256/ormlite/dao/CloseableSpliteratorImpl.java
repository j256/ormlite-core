package com.j256.ormlite.dao;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import com.j256.ormlite.misc.IOUtils;

/**
 * Implementation of {@link CloseableSpliterator} which provides {@link CloseableSpliterator#close()} implementations,
 * and is based on unknown size spliterator (no {@link Spliterator#SIZED} characteristic).
 * 
 * @author zhemaituk
 */
public class CloseableSpliteratorImpl<T> implements CloseableSpliterator<T> {

	private final Spliterator<T> delegate;
	private final CloseableIterator<? extends T> iterator;

	public CloseableSpliteratorImpl(CloseableIterator<? extends T> iterator) {
		this.delegate = Spliterators.spliteratorUnknownSize(iterator, 0);
		this.iterator = iterator;
	}

	@Override
	public void close() throws Exception {
		iterator.close();
	}

	@Override
	public void closeQuietly() {
		IOUtils.closeQuietly(this);
	}

	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		return delegate.tryAdvance(action);
	}

	@Override
	public Spliterator<T> trySplit() {
		return delegate.trySplit();
	}

	@Override
	public long estimateSize() {
		return delegate.estimateSize();
	}

	@Override
	public int characteristics() {
		return delegate.characteristics();
	}
}
