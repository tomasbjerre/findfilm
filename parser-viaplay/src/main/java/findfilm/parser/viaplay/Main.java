package findfilm.parser.viaplay;

import com.google.gson.Gson;

import findfilm.core.domain.Film;

public class Main {
	public static void main(final String[] args) {
		final ViaPlaySource viaplay = new ViaPlaySource();
		for (final String c : viaplay.getCategories()) {
			for (final String film : viaplay.getFilmsInCategory(c)) {
				final Film d = viaplay.getDetailedFilm(film);
				System.out.println(new Gson().toJson(d));
			}
		}
	}
}
