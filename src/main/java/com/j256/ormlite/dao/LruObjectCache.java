package com.j256.ormlite.dao;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Cache for ORMLite which stores a certain number of items. Inserting an object into the cache once it is full will
 * cause the least-recently-used object to be ejected. This cache only stores objects of a single Class. They can be
 * injected into a dao with the {@link Dao#setObjectCache(ObjectCache)}.
 * 
 * @author graywatson
 */
public class LruObjectCache implements ObjectCache {

	private final Class<?> clazz;
	private final Map<Object, Object> objectMap;

	public LruObjectCache(Class<?> clazz, int capacity) {
		this.clazz = clazz;
		this.objectMap = Collections.synchronizedMap(new LimitedLinkedHashMap<Object, Object>(capacity));
	}

	public <T, ID> T get(Class<T> clazz, ID id) {
		if (this.clazz != clazz) {
			throw new IllegalArgumentException("This cache only supports cacheing of " + this.clazz + " objects, not "
					+ clazz);
		}
		Object obj = objectMap.get(id);
		@SuppressWarnings("unchecked")
		T castObj = (T) obj;
		return castObj;
	}

	public <T, ID> void put(Class<T> clazz, ID id, T data) {
		if (this.clazz != clazz) {
			throw new IllegalArgumentException("This cache only supports cacheing of " + this.clazz + " objects, not "
					+ clazz);
		}
		objectMap.put(id, data);
	}

	public void clear() {
		objectMap.clear();
	}

	public <T, ID> void remove(Class<T> clazz, ID id) {
		if (this.clazz != clazz) {
			throw new IllegalArgumentException("This cache only supports cacheing of " + this.clazz + " objects, not "
					+ clazz);
		}
		objectMap.remove(id);
	}

	public <T, ID> T updateId(Class<T> clazz, ID oldId, ID newId) {
		if (this.clazz != clazz) {
			throw new IllegalArgumentException("This cache only supports cacheing of " + this.clazz + " objects, not "
					+ clazz);
		}
		Object obj = objectMap.remove(oldId);
		if (obj == null) {
			return null;
		}
		objectMap.put(newId, obj);
		@SuppressWarnings("unchecked")
		T castObj = (T) obj;
		return castObj;
	}

	public int size() {
		return objectMap.size();
	}

	/**
	 * Little extension of the LimitedLinkedHashMap
	 */
	private static class LimitedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

		private static final long serialVersionUID = -4566528080395573236L;
		private final int capacity;

		public LimitedLinkedHashMap(int capacity) {
			this.capacity = capacity;
		}

		@Override
		protected boolean removeEldestEntry(Entry<K, V> eldest) {
			return size() > capacity;
		}
	}
}
