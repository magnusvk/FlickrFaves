package de.vonkoeller.flickrFaves.debug;

import java.util.Date;

import de.vonkoeller.flickrFaves.gui.Constants;

/**
 * @author Magnus von Koeller
 * 
 *         Called to trace the program's progress. Logs all messages and makes
 *         them available in case of an error.
 */
public final class Tracer {

	/**
	 * Utility class -- cannot be instantiated.
	 */
	private Tracer() {
		// empty on purpose
	}

	private static StringBuffer trace = new StringBuffer();

	public static void trace(String message) {
		message = new Date() + ": " + message;
		if (Constants.DEBUG) {
			System.out.println(message);
		}
		trace.append(message);
		trace.append("\n");
	}

	public static String getTrace() {
		return trace.toString();
	}

}
