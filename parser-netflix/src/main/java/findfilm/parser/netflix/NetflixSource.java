package findfilm.parser.netflix;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static findfilm.core.FindFilmUtils.cleanString;
import static findfilm.core.Logger.INFO;
import static findfilm.core.Logger.log;
import static findfilm.core.PropertyLoader.getStoredProperty;
import static findfilm.parser.job.ParserUtils.get;
import static findfilm.parser.job.ParserUtils.getAllFromPattern;
import static findfilm.parser.job.ParserUtils.scrollAndGetItems;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.collect.ImmutableMap;

import findfilm.core.domain.Film;
import findfilm.core.domain.FilmSourceData;
import findfilm.parser.api.FilmSource;
import findfilm.parser.job.DriverCondition;

public class NetflixSource implements FilmSource {
	private static final String genreLinkStart = "http://www.netflix.com/WiGenre?agid=";
	private static final String genreLinkStartRegExp = "WiGenre\\?agid=([0-9]+)";
	private static final String movieLinkStart = "http://www.netflix.com/WiMovie/";
	private static DriverCondition netflixErrorCheck = webDriver -> webDriver.findElements(
			By.id("siteProblemsPostcard")).isEmpty()
			&& webDriver.findElements(By.id("errorPage")).isEmpty();
	private static final String payLinkStart = "http://www.netflix.com/WiPlayer?movieid=";
	private static final String playLinkStartRegExp = "WiPlayer\\?movieid=([0-9]+)";
	private static final String showTitlePattern = "showTitle\">([^<]*)";
	public static final Map<String, String> VARIANTS = ImmutableMap.<String, String> builder() //
			.put("US", "97.77.104.22:80") //
			.put("SE", "") //
			.build();
	private Optional<String> currentVariant = empty();

	private Optional<WebDriver> webDriver = empty();

	public NetflixSource() {
	}

	@Override
	public List<String> getCategories() {
		final Set<String> allGenres = newHashSet();
		final List<String> mainGenres = getAllFromPattern(webDriver.get(), genreLinkStartRegExp,
				of("http://www.netflix.com/WiHome"), of(netflixErrorCheck));
		for (int i = 0; i < mainGenres.size(); i++) {
			final String it = mainGenres.get(i);
			allGenres.add(it);
			final List<String> subCategories = getAllFromPattern(webDriver.get(), genreLinkStartRegExp,
					of(genreLinkStart + it), of(netflixErrorCheck));
			System.out.println("Found " + subCategories.size() + " sub categories in " + it);
			allGenres.addAll(subCategories);
		}
		return newArrayList(allGenres);
	}

	@Override
	public Film getDetailedFilm(final String it) {
		get(webDriver.get(), movieLinkStart + it, of(netflixErrorCheck));
		final Optional<String> title = this.getTitle(it);
		if (!title.isPresent()) {
			throw new RuntimeException("Unable to get title of " + it);
		}
		final Optional<String> thumbnail = this.getThumbnail(it);
		if (!thumbnail.isPresent()) {
			throw new RuntimeException("Unable to get thumbnail of " + it);
		}
		final String url = payLinkStart + it;
		return new Film(cleanString(title.get())) //
				.withSource(new FilmSourceData(url, thumbnail.get(), getIdentifier(), it));
	}

	@Override
	public List<String> getFilmsInCategory(final String category) {
		get(webDriver.get(), genreLinkStart + category, of(webDriver -> netflixErrorCheck.isOk(webDriver)
				&& !webDriver.findElements(By.id("genrePage")).isEmpty()));
		final List<String> films = scrollAndGetItems(webDriver.get(), playLinkStartRegExp, of(netflixErrorCheck));
		return films;
	}

	@Override
	public String getIdentifier() {
		return "Netflix" + (currentVariant.get().equals("SE") ? "" : "_" + currentVariant.get());
	}

	private Optional<String> getThumbnail(final String it) {
		String value = "";
		final List<WebElement> boxShotImages = this.webDriver.get().findElements(By.className("boxShotImg"));
		for (int i = 0; i < boxShotImages.size(); i++) {
			if (!boxShotImages.get(i).getAttribute("src").isEmpty()) {
				value = boxShotImages.get(i).getAttribute("src").trim().replaceAll("\\.webp", "\\.jpg")
						.replaceAll("\\/webp", "\\/images");
				break;
			}
		}
		return value.isEmpty() ? Optional.empty() : Optional.of(value);
	}

	private Optional<String> getTitle(final String it) {
		try {
			// Wait for title-wrapper to appear
			Thread.sleep(2000);
		} catch (final InterruptedException e) {
		}
		String value = "";
		if (!this.webDriver.get().findElements(By.className("title-wrapper")).isEmpty()) {
			final WebElement titleElement = this.webDriver.get().findElement(By.className("title-wrapper"));
			if (!titleElement.findElements(By.className("origTitle")).isEmpty()) {
				final String origTitle = titleElement.findElements(By.className("origTitle")).get(0).getText();
				value = origTitle.substring(1, origTitle.length() - 1);
			} else {
				value = titleElement.findElement(By.className("title")).getText().trim();
			}
		} else if (!this.webDriver.get().findElements(By.className("showTitle")).isEmpty()) {
			value = getAllFromPattern(webDriver.get(), showTitlePattern, empty(), of(netflixErrorCheck)).get(0).trim();
		} else {
			value = this.webDriver.get().findElement(By.className("showTitle")).getText().trim();
		}
		return value.isEmpty() ? empty() : Optional.of(value);
	}

	@Override
	public List<String> getVariants() {
		return newArrayList(VARIANTS.keySet());
	}

	private void login() {
		get(webDriver.get(), "https://www.netflix.com/Login?locale=sv-SE", of(netflixErrorCheck));
		final WebElement emailElement = this.webDriver.get().findElement(By.id("email"));
		emailElement.sendKeys(getStoredProperty("netflix_email"));
		final WebElement passwordElement = this.webDriver.get().findElement(By.id("password"));
		passwordElement.sendKeys(getStoredProperty("netflix_password"));
		final WebElement submitButton = this.webDriver.get().findElement(By.id("login-form-contBtn"));
		submitButton.click();
	}

	@Override
	public void quit() {
		if (webDriver.isPresent()) {
			this.webDriver.get().quit();
		}
		this.webDriver = empty();
	}

	@Override
	public void setupIfNotSetup(String variant) {
		if (this.webDriver.isPresent() && currentVariant.isPresent() && currentVariant.get().equals(variant)) {
			return;
		}
		final DesiredCapabilities capabilities = new DesiredCapabilities();
		this.currentVariant = of(variant);
		if (!isNullOrEmpty(VARIANTS.get(variant))) {
			final Proxy proxy = new Proxy();
			proxy.setHttpProxy(VARIANTS.get(variant));
			capabilities.setCapability("proxy", proxy);
		}
		this.webDriver = of(new FirefoxDriver(capabilities));
		log(variant + "> " + VARIANTS.get(variant), INFO);
		login();
	}
}
