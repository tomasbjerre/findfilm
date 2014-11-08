package findfilm.parser.job.strategy;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Optional;

import findfilm.core.domain.Film;
import findfilm.core.domain.FilmSourceData;
import findfilm.storage.Storage;

public class FakeStorage implements Storage {
	private static int idCounter = 0;

	public static int incrementId() {
		return idCounter++;
	}

	private List<Film> films;

	@Override
	public void delete(String filmId) {
	}

	@Override
	public Optional<List<Film>> get() {
		return ofNullable(films);
	}

	@Override
	public Optional<Film> get(String filmId) {
		for (final Film f : films) {
			if (f.getId().equals(filmId)) {
				return of(f);
			}
		}
		return empty();
	}

	@Override
	public String getIdentifier() {
		return "TestStorage";
	}

	@Override
	public void init() throws Exception {
	}

	@Override
	public Optional<Film> post(Film film) {
		if (get(film.getId()).isPresent()) {
			throw new RuntimeException("Film exists!");
		}
		films.add(film.withId(incrementId() + ""));
		return of(film);
	}

	@Override
	public Optional<Film> put(Film film) {
		final Optional<Film> foundFilm = get(film.getId());
		if (!foundFilm.isPresent()) {
			throw new RuntimeException("Film does not exists!");
		}
		films.remove(foundFilm.get());
		post(film);
		return of(film);
	}

	@Override
	public List<Film> search(Film searchedFilm) {
		return newArrayList(films
				.stream()
				.filter(filmInStorage -> {
					if (filmInStorage.getTitle().equals(searchedFilm.getTitle())) {
						return true;
					} else {
						if (filmInStorage
								.getSources()
								.stream()
								.anyMatch(
										sourceInStorage -> {
											for (final FilmSourceData searchedFilmSource : searchedFilm.getSources()) {
												if (sourceInStorage.getIdentifier().equals(
														searchedFilmSource.getIdentifier())
														|| sourceInStorage.getFilmSourceId().equals(
																searchedFilmSource.getFilmSourceId())) {
													return true;
												}
											}
											return false;
										})) {
							return true;
						}
					}
					return false;
				}).iterator());
	}

	public FakeStorage setFilms(List<Film> films) {
		this.films = films;
		return this;
	}
}
