package com.j256.ormlite.logger;

import java.util.Arrays;

/**
 * Fluent-context implementation that records the message, throwable, and/or associated arguments and calls through to
 * {@link BaseLogger} to write out the message when the {@link #log()} method is called.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public class FluentContextImpl implements FluentContext {

	private final static int DEFAULT_NUM_ARGS = 4;
	final static String JUST_THROWABLE_MESSAGE = "throwable";

	private final FluentLogger logger;
	private final Level level;
	private String msg;
	/** message builder only used if {@link FluentContext#appendMsg(String)} is called */
	private StringBuilder msgBuilder;
	private Throwable throwable;
	private Object[] args;
	private int argCount;

	public FluentContextImpl(FluentLogger logger, Level level) {
		this.logger = logger;
		this.level = level;
	}

	@Override
	public FluentContext msg(String msg) {
		if (this.msg != null || this.msgBuilder != null || msg == null) {
			// only the first call is honored in case we want to set max arguments
			return this;
		}
		this.msg = msg;

		// get the number of {} arguments to initialize our arguments array
		int count = logger.countArgStrings(msg);
		if (count > 0) {
			if (args == null) {
				args = new Object[count];
			} else if (args.length < count) {
				args = Arrays.copyOf(args, count);
			} else {
				// no point in shrinking it now
			}
		}
		return this;
	}

	@Override
	public FluentContext appendMsg(String msgSuffix) {
		if (msgSuffix == null) {
			// no-op
		} else if (this.msgBuilder != null) {
			this.msgBuilder.append(msgSuffix);
		} else if (this.msg == null) {
			// effectively the same as msg(String)
			this.msg = msgSuffix;
		} else if (msgSuffix.length() > 0) {
			this.msgBuilder = new StringBuilder(this.msg);
			this.msg = null;
			this.msgBuilder.append(msgSuffix);
		}
		return this;
	}

	@Override
	public FluentContext throwable(Throwable throwable) {
		if (this.throwable == null) {
			this.throwable = throwable;
		}
		return this;
	}

	@Override
	public FluentContext arg(Object arg) {
		addArg(arg);
		return this;
	}

	@Override
	public FluentContext arg(boolean arg) {
		addArg(arg);
		return this;
	}

	@Override
	public FluentContext arg(byte arg) {
		addArg(arg);
		return this;
	}

	@Override
	public FluentContext arg(char arg) {
		addArg(arg);
		return this;
	}

	@Override
	public FluentContext arg(short arg) {
		addArg(arg);
		return this;
	}

	@Override
	public FluentContext arg(int arg) {
		addArg(arg);
		return this;
	}

	@Override
	public FluentContext arg(long arg) {
		addArg(arg);
		return this;
	}

	@Override
	public FluentContext arg(float arg) {
		addArg(arg);
		return this;
	}

	@Override
	public FluentContext arg(double arg) {
		addArg(arg);
		return this;
	}

	@Override
	public FluentContext args(Object[] addArgs) {
		if (addArgs == null) {
			return this;
		}
		if (this.args == null) {
			// NOTE: this will reuse the args argument but only until the log() call or until another arg is added
			args = addArgs;
			argCount = addArgs.length;
		} else {
			// extend the array if necessary
			if (args.length - argCount < addArgs.length) {
				this.args = Arrays.copyOf(args, argCount + addArgs.length);
			}
			for (int i = 0; i < addArgs.length; i++) {
				args[argCount++] = addArgs[i];
			}
		}
		return this;
	}

	@Override
	public void log() {
		String msgToPrint;
		if (msgBuilder == null) {
			msgToPrint = msg;
		} else {
			msgToPrint = msgBuilder.toString();
		}
		if (msgToPrint == null) {
			// if we have no message but we do have arguments then build a message like: '{}', '{}', ...
			if (argCount > 0) {
				logger.doLog(level, throwable, null, args, argCount);
			} else if (throwable == null) {
				// ignore log line if no message, args, or throwable
			} else {
				// just log a throwable with a minimal message
				logger.doLog(level, throwable, JUST_THROWABLE_MESSAGE, null, 0);
			}
		} else if (argCount == 0) {
			// no arguments
			logger.doLog(level, throwable, msgToPrint, null, 0);
		} else {
			logger.doLog(level, throwable, msgToPrint, args, argCount);
		}
		// chances are we are done with the object after this
	}

	private void addArg(Object arg) {
		if (args == null) {
			args = new Object[DEFAULT_NUM_ARGS];
		} else if (argCount >= args.length) {
			// whenever we grow the array we double it
			args = Arrays.copyOf(args, args.length * 2);
		}
		args[argCount++] = arg;
	}
}
