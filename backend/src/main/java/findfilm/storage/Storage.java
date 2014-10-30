package findfilm.storage;

import java.util.List;
import java.util.Optional;

import findfilm.core.domain.Film;

public interface Storage {
	public void delete(String filmId);

	public Optional<List<Film>> get();

	public Optional<Film> get(String filmId);

	public String getIdentifier();

	public void init() throws Exception;

	/**
	 * Create
	 */
	public Optional<Film> post(Film film);

	/**
	 * Update
	 */
	public Optional<Film> put(Film film);

	public List<Film> search(Film film);
}
