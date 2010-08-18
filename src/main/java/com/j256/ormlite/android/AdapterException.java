package com.j256.ormlite.android;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Aug 17, 2010
 * Time: 10:27:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class AdapterException extends Exception
{
	private static final long serialVersionUID = 6557739848118504299L;

	public AdapterException()
    {
    }

    public AdapterException(String s)
    {
        super(s);
    }

    public AdapterException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public AdapterException(Throwable throwable)
    {
        super(throwable);
    }
}
