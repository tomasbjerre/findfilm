package findfilm.parser.api;

import java.util.List;

import findfilm.core.domain.Film;

public interface FilmSource {
	/**
	 * Get all available categories
	 */
	public List<String> getCategories();

	/**
	 * Get all available details on given film
	 */
	public Film getDetailedFilm(String id);

	/**
	 * Get all films in given category
	 */
	public List<String> getFilmsInCategory(String category);

	/**
	 * @return An identifier string for the parser.
	 */
	public String getIdentifier();

	public List<String> getVariants();

	public void quit();

	public void setupIfNotSetup(String variant);
}
