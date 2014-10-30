package findfilm.core;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static java.nio.file.Files.newInputStream;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertyLoader {
	private static final Path file = Paths.get("/etc/pleasedonthackme.properties");
	private static Properties prop = new Properties();

	static {
		try {
			final InputStream stream = newInputStream(file);
			prop.load(stream);
			stream.close();
		} catch (final Exception e) {
			propagate(e);
		}
	}

	public static String getStoredProperty(final String key) {
		return checkNotNull(prop.getProperty(key), "Could not find \"" + key + "\" in "
				+ file.toFile().getAbsoluteFile().getPath());
	}

	private PropertyLoader() {

	}
}
