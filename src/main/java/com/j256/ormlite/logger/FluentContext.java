package com.j256.ormlite.logger;

/**
 * Context for our fluent logger calls that is returned by a call to {@link FluentLogger#atLevel(Level)}. The
 * {@link #msg(String)} method should be called once to set the message format for the log output. To end the chain and
 * write out the message you should call the {@link #log()} method.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public interface FluentContext {

	/**
	 * Set the required log message on the context. If there has been a previous call to this method or
	 * {@link #appendMsg(String)} then this method call is ignored. Optimizations may happen if this method is called
	 * _before_ any arg(...) or args(...) methods but this is not required.
	 */
	public FluentContext msg(String message);

	/**
	 * Append the message argument to any previously set message whether there be none or from a call to {@link #msg} or
	 * {@link #appendMsg(String)}. This is less efficient than a single call to {@link #msg(String)} because of the use
	 * of a {@link StringBuilder} under the covers but only if the logger is enabled.
	 */
	public FluentContext appendMsg(String message);

	/**
	 * Set the optional throwable on the context. Only the first call to this method is honored.
	 */
	public FluentContext throwable(Throwable th);

	/**
	 * Add a single object argument to the log message. This can also be an array of objects if you want them to match a
	 * single {} from the message and displayed as "[ele1, ele2, ...]". For more information, see
	 * {@link #args(Object[])}.
	 */
	public FluentContext arg(Object arg);

	/**
	 * Add boolean primitive argument to the log message.
	 */
	public FluentContext arg(boolean arg);

	/**
	 * Add byte primitive argument to the log message.
	 */
	public FluentContext arg(byte arg);

	/**
	 * Add char primitive argument to the log message.
	 */
	public FluentContext arg(char arg);

	/**
	 * Add short primitive argument to the log message.
	 */
	public FluentContext arg(short arg);

	/**
	 * Add int primitive argument to the log message.
	 */
	public FluentContext arg(int arg);

	/**
	 * Add long primitive argument to the log message.
	 */
	public FluentContext arg(long arg);

	/**
	 * Add float primitive argument to the log message.
	 */
	public FluentContext arg(float arg);

	/**
	 * Add double primitive argument to the log message.
	 */
	public FluentContext arg(double arg);

	/**
	 * Add an array of object arguments to the log message, each element of which will match a single {} from the
	 * message. To add an array to be associated with a single {} and displayed as {@code [arg1, arg2, ...]} then you
	 * need to use the method {@link #arg(Object)} which will interpret the array as a single object.
	 * 
	 * For example, the following code calls this args(...) method:
	 * 
	 * <pre>
	 * // this outputs: 1 + 2 = 3
	 * fluentLogger.msg("{} + {} = {}").args(new Object[] { 1, 2, 3 }).log();
	 * </pre>
	 * 
	 * While this code calls {@link #arg(Object)} which interprets the array as an @code{Object} and will match a single
	 * {} from the message:
	 * 
	 * <pre>
	 * // this outputs: port numbers: [1, 2, 3]
	 * fluentLogger.msg("port numbers: {}").arg(new Object[] { 1, 2, 3 }).log();
	 * </pre>
	 * 
	 * <b>NOTE:</b> this will reuse the args argument but only until the log() call or until another arg is added.
	 */
	public FluentContext args(Object[] args);

	/**
	 * Log the message to output if the level is enabled. Must be at the end of the method call chain.
	 */
	public void log();
}
