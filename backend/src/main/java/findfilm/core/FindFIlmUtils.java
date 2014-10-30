package findfilm.core;

public class FindFIlmUtils {
	public static String cleanString(final String string) {
		return string.trim().replaceAll("[^a-zåäöA-ZÅÄÖ0-9- ]", "");
	}
}
