package com.j256.ormlite.field;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used to manage the various data types used by the system. The bulk of the data types come from the
 * {@link DataType} enumerated fields although you can also register your own here using the
 * {@link #registerDataTypes(DataPersister...)} method.
 * 
 * @author graywatson
 */
public class DataPersisterManager {

	private static Map<Class<?>, DataPersister> classMap = null;

	/**
	 * Register a data type with the manager.
	 */
	public static void registerDataTypes(DataPersister... dataPersisters) {
		HashMap<Class<?>, DataPersister> newMap = new HashMap<Class<?>, DataPersister>();
		if (classMap != null) {
			newMap.putAll(classMap);
		}
		for (DataPersister dataPersister : dataPersisters) {
			if (dataPersister != null) {
				for (Class<?> clazz : dataPersister.getAssociatedClasses()) {
					newMap.put(clazz, dataPersister);
				}
			}
		}
		// replace the map to lower the chance of concurrency issues
		classMap = newMap;
	}

	/**
	 * Lookup the data-type associated with the class.
	 * 
	 * @return The associated data-type interface or null if none found.
	 */
	public static DataPersister lookupForClass(Class<?> dataClass) {

		if (classMap == null) {
			// this will create a new map
			DataType[] dataTypes = DataType.values();
			DataPersister[] dataPersisters = new DataPersister[dataTypes.length];
			for (int i = 0; i < dataPersisters.length; i++) {
				dataPersisters[i] = dataTypes[i].getDataPersister();
			}
			registerDataTypes(dataPersisters);
		}

		// first look it up in our map
		DataPersister dataType = classMap.get(dataClass);
		if (dataType != null) {
			return dataType;
		}

		if (dataClass.isEnum()) {
			// special handling of the Enum type
			return DataType.ENUM_STRING.getDataPersister();
		} else {
			return null;
		}
	}

	public static Collection<DataPersister> getDataPersisters() {
		if (classMap == null) {
			return Collections.emptySet();
		} else {
			return classMap.values();
		}
	}
}
