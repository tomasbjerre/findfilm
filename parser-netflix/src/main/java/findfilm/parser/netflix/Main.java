package findfilm.parser.netflix;

import static findfilm.parser.netflix.NetflixSource.VARIANTS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import findfilm.core.domain.Film;

public class Main {
	public static void main(final String[] args) throws MalformedURLException, IOException, Exception {
		final NetflixSource netflixSource = new NetflixSource();
		for (final String variant : VARIANTS.keySet()) {
			netflixSource.setupIfNotSetup(variant);
			final List<String> categories = netflixSource.getCategories();
			System.out.println("Found " + categories.size() + " categories");
			for (final String category : categories) {
				final List<String> filmResult = netflixSource.getFilmsInCategory(category);
				System.out.println("Found " + filmResult.size());
				for (final String filmId : filmResult) {
					final Film film = netflixSource.getDetailedFilm(filmId);
					System.out.println(film.getTitle());
				}
			}
		}
		netflixSource.quit();
	}
}
