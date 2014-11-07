package findfilm.core;

import static java.util.Optional.empty;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class FindFilmUtils {
	private static Optional<String> fakeDate = empty();
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public static String cleanString(final String string) {
		return string.trim().replaceAll("[^a-zåäöA-ZÅÄÖ0-9- ]", "");
	}

	public static void setFakeToday(Optional<String> fakeDate) {
		FindFilmUtils.fakeDate = fakeDate;
	}

	public static String today() {
		if (fakeDate.isPresent()) {
			return fakeDate.get();
		}
		return sdf.format(new Date());
	}
}
