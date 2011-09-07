package com.j256.ormlite.dao;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Default cache for ORMLite which uses {@link WeakReference} around the value in the cache to store objects. When there
 * are GCs, any object that does not have a hard external reference will be set to null and cleared out of the cache.
 * 
 * @author graywatson
 */
public class LruObjectCache implements ObjectCache {

	private final Map<Object, Object> objectMap;

	public LruObjectCache(int capacity) {
		this.objectMap = Collections.synchronizedMap(new LimitedLinkedHashMap<Object, Object>(capacity));
	}

	public <T, ID> T get(Class<T> clazz, ID id) {
		Object obj = objectMap.get(id);
		@SuppressWarnings("unchecked")
		T castObj = (T) obj;
		return castObj;
	}

	public <T, ID> void put(Class<T> clazz, ID id, T data) {
		objectMap.put(id, data);
	}

	public void clear() {
		objectMap.clear();
	}

	public <T, ID> void remove(Class<T> clazz, ID id) {
		objectMap.remove(id);
	}

	public <T, ID> T updateId(Class<T> clazz, ID oldId, ID newId) {
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
