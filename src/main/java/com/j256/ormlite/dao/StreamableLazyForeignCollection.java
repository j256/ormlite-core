package com.j256.ormlite.dao;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.IOUtils;

/**
 * Lazy foreign collection class that only works with JDK8+ because of the Stream usage. Fortuitously this can be
 * compiled under JDK6 even with the unknown imports as long as it doesn't use lambdas or any other language features.
 * This allows ORMLite to stay compatible with Java6 class files but provide better stream support for Java8+.
 * 
 * We are trying to fix the below code issue where Account.orders is a {@link LazyForeignCollection}.
 * 
 * <pre>
 * try (Stream<Document> stream = account.getOrders().stream();) {
 *    Order firstOrder = stream.findFirst().orElse(null);
 * }
 * </pre>
 * 
 * <p>
 * Without this class, the spliterator that is created by the {@link #spliterator()} method has the
 * {@link Spliterator#SIZED} characteristic enabled implying that the collection is in memory and it is cheap to
 * estimate its size -- with a lazy collection, this can be very expensive causing the collection to be walked once
 * unnecessarily. Also, this class returns a {@link Stream} from the {@link #stream()} method that uses a
 * {@link CloseableWrappedIterable} and registers a {@link Stream#onClose()} runnable to ensure that the iterator is
 * properly closed once the {@link Stream#close()} method is called.
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
		final CloseableWrappedIterableImpl<T> iterable = new CloseableWrappedIterableImpl<T>(this);
		/*
		 * NOTE: we have to use the Runnable here because this has to compile via the JDK6 compiler which doesn't
		 * understand lambdas but which seems to ignore the missing imports only available under JDK8+.
		 */
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterable.iterator(), 0), false)
				.onClose(new Runnable() {
					@Override
					public void run() {
						IOUtils.closeQuietly(iterable);
					}
				});
	}
}
