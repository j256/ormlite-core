package com.j256.ormlite.dao;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for ORMLite which stores objects with a {@link WeakReference} or {@link SoftReference} to them. Java Garbage
 * Collection can then free these objects if no one has a "strong" reference to the object (weak) or if it runs out of
 * memory (soft). This This cache only stores objects of a single Class.
 * 
 * @author graywatson
 */
public class ReferenceObjectCache implements ObjectCache {

	private final ConcurrentHashMap<Object, Reference<Object>> objectMap =
			new ConcurrentHashMap<Object, Reference<Object>>();
	private final Class<?> clazz;
	private final boolean useWeak;

	/**
	 * @param useWeak
	 *            Set to true if you want the cache to use {@link WeakReference}. If false then the cache will use
	 *            {@link SoftReference}.
	 */
	public ReferenceObjectCache(Class<?> clazz, boolean useWeak) {
		this.clazz = clazz;
		this.useWeak = useWeak;
	}

	/**
	 * Create and return an object cache using {@link WeakReference}.
	 */
	public static ReferenceObjectCache makeWeakCache(Class<?> clazz) {
		return new ReferenceObjectCache(clazz, true);
	}

	/**
	 * Create and return an object cache using {@link SoftReference}.
	 */
	public static ReferenceObjectCache makeSoftCache(Class<?> clazz) {
		return new ReferenceObjectCache(clazz, false);
	}

	public <T, ID> T get(Class<T> clazz, ID id) {
		if (this.clazz != clazz) {
			throw new IllegalArgumentException("This cache only supports cacheing of " + this.clazz + " objects, not "
					+ clazz);
		}
		Reference<Object> ref = objectMap.get(id);
		if (ref == null) {
			return null;
		}
		Object obj = ref.get();
		if (obj == null) {
			objectMap.remove(id);
			return null;
		} else {
			@SuppressWarnings("unchecked")
			T castObj = (T) obj;
			return castObj;
		}
	}

	public <T, ID> void put(Class<T> clazz, ID id, T data) {
		if (this.clazz != clazz) {
			throw new IllegalArgumentException("This cache only supports cacheing of " + this.clazz + " objects, not "
					+ clazz);
		}
		if (useWeak) {
			objectMap.put(id, new WeakReference<Object>(data));
		} else {
			objectMap.put(id, new SoftReference<Object>(data));
		}
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
		Reference<Object> ref = objectMap.remove(oldId);
		if (ref == null) {
			return null;
		}
		objectMap.put(newId, ref);
		@SuppressWarnings("unchecked")
		T castObj = (T) ref.get();
		return castObj;
	}

	public int size() {
		return objectMap.size();
	}

	/**
	 * Run through the map and remove any references that have been null'd out by the GC.
	 */
	public void cleanNullReferences() {
		Iterator<Entry<Object, Reference<Object>>> iterator = objectMap.entrySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getValue().get() == null) {
				iterator.remove();
			}
		}
	}
}
