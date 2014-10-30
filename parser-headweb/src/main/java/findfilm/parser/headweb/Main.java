package findfilm.parser.headweb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import findfilm.core.domain.Film;

public class Main {
	public static void main(final String[] args) throws MalformedURLException, IOException, Exception {
		final HeadWebSource netflixSource = new HeadWebSource();
		netflixSource.setupIfNotSetup("SE");
		final List<String> categories = netflixSource.getCategories();
		System.out.println("Found " + categories.size() + " categories");
		for (final String c : categories) {
			System.out.println(c);
		}
		for (final String category : categories) {
			final List<String> filmResult = netflixSource.getFilmsInCategory(category);
			System.out.println("Found " + filmResult.size());
			for (final String filmId : filmResult) {
				final Film film = netflixSource.getDetailedFilm(filmId);
				System.out.println(film.getTitle());
			}
		}
		netflixSource.quit();
	}
}
