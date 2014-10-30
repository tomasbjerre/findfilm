package findfilm.parser.job;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Thread.sleep;
import static java.util.Optional.empty;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class ParserUtils {
	public static void get(final WebDriver webDriver, final String url, Optional<DriverCondition> condition) {
		for (int i = 0; i < 100; i++) {
			try {
				webDriver.get(url);
				if (!condition.isPresent() || condition.isPresent() && condition.get().isOk(webDriver)) {
					return;
				}
				sleepIncr(i);
			} catch (final Exception e) {
				// May be a temporary 404 !
				e.printStackTrace();
			}
		}
		System.out.println("Giving up on " + url);
	}

	public static List<String> getAllFromPattern(final WebDriver webDriver, final String pattern,
			final Optional<String> url, Optional<DriverCondition> driverCondition) {
		final List<String> list = newArrayList();
		final Pattern p = Pattern.compile(pattern);
		for (int c = 0; c < 10 && list.isEmpty(); c++) {
			if (url.isPresent()) {
				get(webDriver, url.get(), driverCondition);
			}
			final Matcher m = p.matcher(webDriver.getPageSource());
			while (m.find()) {
				if (m.group(1).isEmpty()) {
					String s = "";
					for (int i = 0; i < m.groupCount(); i++) {
						s += " | " + m.group(i);
					}
					throw new RuntimeException("Found empty match for " + pattern + " in: " + webDriver.getCurrentUrl()
							+ " " + s);
				}
				list.add(m.group(1));
			}
			if (list.isEmpty()) {
				try {
					sleep(2000);
				} catch (final InterruptedException e) {
				}
			}
		}
		return list;
	}

	public static List<String> scrollAndGetItems(final WebDriver webDriver, final String regExp,
			Optional<DriverCondition> driverCondition) {
		for (int j = 0; j < 1000; j++) {
			final List<String> itemsBefore = getAllFromPattern(webDriver, regExp, empty(), driverCondition);
			scrollDown(webDriver);
			try {
				sleep(1000);
			} catch (final Exception e) {
			}
			if (itemsBefore.size() == getAllFromPattern(webDriver, regExp, empty(), driverCondition).size()) {
				return itemsBefore;
			}
		}
		return newArrayList();
	}

	public static void scrollDown(final WebDriver webDriver) {
		final JavascriptExecutor jsx = (JavascriptExecutor) webDriver;
		jsx.executeScript("window.scrollBy(0,10000)", "");
	}

	private static void sleepIncr(int i) {
		try {
			if (i == 0) {
				sleep(200);
			} else if (i == 1) {
				sleep(500);
			} else if (i == 2) {
				sleep(1000);
			} else if (i == 3) {
				sleep(2000);
			} else {
				sleep(10000);
			}
		} catch (final InterruptedException e) {
		}
	}
}
