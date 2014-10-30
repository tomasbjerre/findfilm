package findfilm.core;

import static findfilm.core.PropertyLoader.getStoredProperty;

public class Logger {

	public static final String DEBUG = "DEBUG";
	public static final String INFO = "INFO";

	public static void log(final String string, final String level) {
		switch (getStoredProperty("loglevel")) {
		case DEBUG:
			System.out.println(string);
			break;
		case INFO:
			if (level.equals(INFO)) {
				System.out.println(string);
			}
			break;
		}
	}

}
