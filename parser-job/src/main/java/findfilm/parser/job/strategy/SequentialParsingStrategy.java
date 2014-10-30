package findfilm.parser.job.strategy;

import static com.google.common.collect.Lists.newArrayList;
import static findfilm.core.Logger.INFO;
import static findfilm.core.Logger.log;

import java.util.List;

import findfilm.core.domain.Film;
import findfilm.core.domain.FilmSourceData;
import findfilm.parser.api.FilmSource;
import findfilm.storage.Storage;

public class SequentialParsingStrategy implements ParsingStrategy {
	public SequentialParsingStrategy(final List<FilmSource> sourcesWithCategories, final Storage storage)
			throws Exception {
		for (final FilmSource filmSource : sourcesWithCategories) {
			log("Using source: " + filmSource.getClass(), INFO);
			int totalFilmsFound = 0;
			int newFilmsFound = 0;
			for (final String variant : filmSource.getVariants()) {
				List<String> categories = newArrayList();
				try {
					filmSource.setupIfNotSetup(variant);
					categories = filmSource.getCategories();
				} catch (final Exception e) {
					e.printStackTrace();
					filmSource.quit();
					log("Exception during category lookup of " + filmSource.getIdentifier() + ", ignoring source.",
							INFO);
					continue;
				}
				for (int i = 0; i < categories.size(); i++) {
					List<String> filmsInCategory = newArrayList();
					try {
						filmSource.setupIfNotSetup(variant);
						filmsInCategory = filmSource.getFilmsInCategory(categories.get(i));
					} catch (final Exception e) {
						e.printStackTrace();
						filmSource.quit();
						log("Exception during films lookup of category " + filmSource.getIdentifier() + "/"
								+ categories.get(i) + ", ignoring category.", INFO);
						continue;
					}

					for (int f = 0; f < filmsInCategory.size(); f++) {
						try {
							filmSource.setupIfNotSetup(variant);
							newFilmsFound += getFilm(storage, filmSource, filmsInCategory.get(f));
						} catch (final Exception e) {
							e.printStackTrace();
							filmSource.quit();
							log("Exception during film lookup of " + filmSource.getIdentifier() + "/"
									+ categories.get(i) + "/" + filmsInCategory.get(f) + ", ignoring film.", INFO);
							continue;
						}
					}

					log(filmSource.getIdentifier() + "> Found " + filmsInCategory.size() + " films in "
							+ categories.get(i), INFO);
					totalFilmsFound += filmsInCategory.size();
					log("Completed " + (i + 1) + " categories of " + categories.size() + ". Found " + totalFilmsFound
							+ " so far and " + newFilmsFound + " were new.", INFO);
				}
				filmSource.quit();
			}
			log("Completed all film sources.", INFO);
		}
	}

	private int getFilm(final Storage storage, final FilmSource filmSource, final String filmSourceId) {
		final List<Film> searchBySource = storage.search( //
				new Film() //
						.withSource( //
						new FilmSourceData()//
								.withIdentifier(filmSource.getIdentifier()) //
								.withFilmSourceId(filmSourceId)));
		if (!searchBySource.isEmpty()) {
			// Will update "last seen"
			storage.put(searchBySource.get(0));
			log("~ " + searchBySource.get(0).getTitle(), INFO);
		} else {
			final Film film = filmSource.getDetailedFilm(filmSourceId);
			final List<Film> searchByTitle = storage.search(new Film(film.getTitle()));
			if (!searchByTitle.isEmpty()) {
				storage.put( //
				searchByTitle.get(0) //
						.withSource( //
								film.getSources().get(0)));
				log("~+ " + film.getTitle(), INFO);
			} else {
				storage.post(film);
				log("+ " + film.getTitle(), INFO);
				return 1;
			}
		}
		return 0;
	}
}
