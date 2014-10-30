package findfilm.storage.impl;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.of;

import java.util.List;
import java.util.Optional;

import findfilm.core.domain.Film;
import findfilm.storage.Storage;

public class DummyStorage implements Storage {
	public DummyStorage() {
	}

	@Override
	public void delete(final String filmId) {
		System.out.println("DELETE> " + filmId);
	}

	@Override
	public Optional<List<Film>> get() {
		System.out.println("GET> ");
		return of(newArrayList());
	}

	@Override
	public Optional<Film> get(final String filmId) {
		System.out.println("GET> " + filmId);
		return Optional.empty();
	}

	@Override
	public String getIdentifier() {
		return "DUMMY";
	}

	@Override
	public void init() throws Exception {
	}

	@Override
	public Optional<Film> post(final Film film) {
		System.out.println("POST> " + film.getTitle());
		return Optional.empty();
	}

	@Override
	public Optional<Film> put(final Film film) {
		System.out.println("PUT> " + film.getTitle());
		return Optional.empty();
	}

	@Override
	public List<Film> search(final Film film) {
		System.out.println("SEARCH> " + film.getTitle());
		return newArrayList();
	}
}
