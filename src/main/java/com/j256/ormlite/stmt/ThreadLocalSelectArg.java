package com.j256.ormlite.stmt;

/**
 * Like {@link SelectArg} but using a {@link ThreadLocal} internally to improve reentrance so that multiple threads can
 * use the same compiled statement.
 * 
 * @author graywatson
 */
public class ThreadLocalSelectArg extends BaseSelectArg implements ArgumentHolder {

	private ThreadLocal<ValueWrapper> threadValue = new ThreadLocal<ValueWrapper>();

	public ThreadLocalSelectArg() {
		// value set later
	}

	public ThreadLocalSelectArg(Object value) {
		setValue(value);
	}

	@Override
	protected Object getValue() {
		ValueWrapper wrapper = threadValue.get();
		if (wrapper == null) {
			return null;
		} else {
			return wrapper.value;
		}
	}

	@Override
	public void setValue(Object value) {
		threadValue.set(new ValueWrapper(value));
	}

	@Override
	protected boolean isValueSet() {
		return threadValue.get() != null;
	}

	/**
	 * Value wrapper so we can differentiate between a null value and no value.
	 */
	private static class ValueWrapper {
		Object value;
		public ValueWrapper(Object value) {
			this.value = value;
		}
	}
}
