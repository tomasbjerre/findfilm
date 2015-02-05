package findfilm.parser.headweb;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static findfilm.core.Logger.INFO;
import static findfilm.core.Logger.log;
import static findfilm.parser.job.ParserUtils.get;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.tagName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import findfilm.core.domain.Film;
import findfilm.core.domain.FilmSourceData;
import findfilm.parser.api.FilmSource;

public class HeadWebSource implements FilmSource {
	private final Map<String, Film> films = newHashMap();
	private final Map<String, List<Film>> filmsByCategory = newHashMap();
	private Optional<WebDriver> webDriver = empty();

	public HeadWebSource() {
	}

	private List<String> getAllPages(String string) {
		final List<String> list = newArrayList();
		for (int i = 1; true; i++) {
			final String category = string + "&page=" + i;
			get(webDriver.get(), category, empty());
			final int sizeBefore = films.size();
			getFilms(category);
			if (films.size() == sizeBefore) {
				break;
			}
			log("Found category " + category, INFO);
			list.add(category);
		}
		return list;
	}

	@Override
	public List<String> getCategories() {
		return newArrayList(concat(getAllPages("https://www.headweb.com/sv/5906/filmer?orderBy=18"),
				getAllPages("https://www.headweb.com/sv/350310/tv-serier"),
				getAllPages("https://www.headweb.com/sv/100391/genre/familj")));
	}

	@Override
	public Film getDetailedFilm(final String it) {
		return films.get(it);
	}

	private void getFilms(String category) {
		final List<WebElement> coverEntries = webDriver.get().findElements(className("coverEntry"));
		for (final WebElement coverelement : coverEntries) {
			final String filmSourceId = coverelement.findElement(tagName("a")).getAttribute("href").split(";")[0];
			final String url = filmSourceId;
			final String img = coverelement.findElement(By.tagName("img")).getAttribute("src");
			final String title = coverelement.findElement(By.tagName("img")).getAttribute("alt");
			final Film film = new Film(title) //
					.withSource( //
					new FilmSourceData() //
							.withFilmSourceId(filmSourceId) //
							.withIdentifier(getIdentifier()) //
							.withThumbnail(img) //
							.withUrl(url) //
					);
			this.films.put(filmSourceId, film);
			if (filmsByCategory.containsKey(category)) {
				filmsByCategory.get(category).add(film);
			} else {
				this.filmsByCategory.put(category, newArrayList(film));
			}
		}
	}

	@Override
	public List<String> getFilmsInCategory(final String category) {
		return newArrayList(transform(filmsByCategory.get(category), //
				input -> input.getSources().get(0).getFilmSourceId()));
	}

	@Override
	public String getIdentifier() {
		return "HeadWeb";
	}

	@Override
	public List<String> getVariants() {
		return newArrayList("SE");
	}

	@Override
	public void quit() {
		if (webDriver.isPresent()) {
			this.webDriver.get().quit();
		}
		this.webDriver = empty();
	}

	private void setup() {
		this.webDriver = of(new FirefoxDriver());
	}

	@Override
	public void setupIfNotSetup(String variant) {
		if (!this.webDriver.isPresent()) {
			setup();
		}
	}
}
