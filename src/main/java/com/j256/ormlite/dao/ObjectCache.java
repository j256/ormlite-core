package com.j256.ormlite.dao;

/**
 * Definition of an object cache that can be injected into the Dao with the {@link Dao#enableObjectCache(boolean)}.
 * 
 * @author graywatson
 */
public interface ObjectCache {

	/**
	 * Lookup in the cache for an object of a certain class that has a certain id.
	 * 
	 * @return The found object or null if none.
	 */
	public <T, ID> T get(Class<T> clazz, ID id);

	/**
	 * Put an object in the cache that has a certain class and id.
	 */
	public <T, ID> void put(Class<T> clazz, ID id, T data);

	/**
	 * Delete from the cache an object of a certain class that has a certain id.
	 */
	public <T, ID> void remove(Class<T> clazz, ID id);

	/**
	 * Change the id in the cache for an object of a certain class from an old-id to a new-id.
	 */
	public <T, ID> T updateId(Class<T> clazz, ID oldId, ID newId);

	/**
	 * Remove all entries from the cache.
	 */
	public void clear();

	/**
	 * Return the number of elements in the cache.
	 */
	public int size();
}
