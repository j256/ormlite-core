package com.j256.ormlite.stmt;

/**
 * An argument to a select SQL statement. After the query is constructed, the caller can set the value on this argument
 * and run the query. Then the argument can be set again and the query re-executed. This is equivalent in SQL to a ?
 * argument.
 * 
 * <p>
 * NOTE: If the argument has not been set by the time the query is executed, an exception will be thrown.
 * </p>
 * 
 * <p>
 * NOTE: For protections sake, the object cannot be reused with different column names.
 * </p>
 * 
 * @author graywatson
 */
public class SelectArg extends BaseSelectArg implements ArgumentHolder {

	private boolean hasBeenSet = false;
	private Object value = null;

	@Override
	protected Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		this.hasBeenSet = true;
		this.value = value;
	}

	@Override
	protected boolean isValueSet() {
		return hasBeenSet;
	}
}
