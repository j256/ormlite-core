package android.util;

/**
 * Stub implementation of the Android log class designed to stop my IDE from being red.
 * 
 * @author graywatson
 */
public class Log {

	public static int VERBOSE = 2;
	public static int DEBUG = 3;
	public static int INFO = 4;
	public static int WARN = 5;
	public static int ERROR = 6;
	
	public static boolean isLoggable(String tag, int level) {
		return false;
	}
	
	public static int v(String tag, String message) {
		return 0;
	}
	
	public static int v(String tag, String message, Throwable t) {
		return 0;
	}
	
	public static int d(String tag, String message) {
		return 0;
	}
	
	public static int d(String tag, String message, Throwable t) {
		return 0;
	}
	
	public static int i(String tag, String message) {
		return 0;
	}
	
	public static int i(String tag, String message, Throwable t) {
		return 0;
	}
	
	public static int w(String tag, String message) {
		return 0;
	}
	
	public static int w(String tag, String message, Throwable t) {
		return 0;
	}
	
	public static int e(String tag, String message) {
		return 0;
	}
	
	public static int e(String tag, String message, Throwable t) {
		return 0;
	}
}
