package com.j256.ormlite.dao;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.IOUtils;

/**
 * Lazy foreign collection that provides better support for {@link Stream}s if running with JDK8+. Fortuitously this can
 * be compiled under JDK6 even with the unknown imports as long as the code doesn't use lambdas or any newer language
 * features. This allows ORMLite to stay compatible with older class files but still provide better stream support for
 * Java8+.
 * 
 * <p>
 * We are trying to fix the below code issue where Account.orders is a {@link LazyForeignCollection}.
 * </p>
 * 
 * <pre>
 * try (Stream<Document> stream = account.getOrders().stream();) {
 *    Order firstOrder = stream.findFirst().orElse(null);
 * }
 * </pre>
 * 
 * <p>
 * Without this class, the spliterator that is returned by {@link #spliterator()} has the {@link Spliterator#SIZED}
 * characteristic enabled which implies that the collection is in memory and it is cheap to estimate its size -- with a
 * lazy collection, this can be very expensive and it causes the collection to be iterated across an additional time
 * unnecessarily. Also, the {@link Stream} returned from the {@link #stream()} method registers a
 * {@link Stream#onClose()} runnable to ensure that the iterator is properly closed once the {@link Stream#close()}
 * method is called.
 * </p>
 * 
 * @author graywatson
 */
public class StreamableLazyForeignCollection<T, ID> extends LazyForeignCollection<T, ID> {

	private static final long serialVersionUID = 1288122099601287859L;

	public StreamableLazyForeignCollection(Dao<T, ID> dao, Object parent, Object parentId, FieldType foreignFieldType,
			String orderColumn, boolean orderAscending) {
		super(dao, parent, parentId, foreignFieldType, orderColumn, orderAscending);
	}

	@Override
	public Spliterator<T> spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), 0);
	}

	@Override
	public Stream<T> stream() {
		final CloseableIterator<T> iterator = closeableIterator();
		/*
		 * NOTE: we have to use a Runnable here because was want to compile via the JDK6 compiler which doesn't
		 * understand lambdas.
		 */
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false).onClose(new Runnable() {
			@Override
			public void run() {
				IOUtils.closeQuietly(iterator);
			}
		});
	}
}
