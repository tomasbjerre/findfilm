package findfilm.parser.job.strategy;

import java.util.List;
import java.util.Map;

import findfilm.core.domain.Film;
import findfilm.parser.api.FilmSource;

public class FakeFilmSource implements FilmSource {

	private List<String> categories;
	private Map<String, Film> detailedFilm;
	private Map<String, List<String>> filmsInCategory;
	private List<String> variants;

	@Override
	public List<String> getCategories() {
		return categories;
	}

	@Override
	public Film getDetailedFilm(String id) {
		return detailedFilm.get(id);
	}

	@Override
	public List<String> getFilmsInCategory(String category) {
		return filmsInCategory.get(category);
	}

	@Override
	public String getIdentifier() {
		return "TestIdentifier";
	}

	@Override
	public List<String> getVariants() {
		return variants;
	}

	@Override
	public void quit() {

	}

	public FakeFilmSource setCategories(List<String> categories) {
		this.categories = categories;
		return this;
	}

	public FakeFilmSource setDetailedFilm(Map<String, Film> detailedFilm) {
		this.detailedFilm = detailedFilm;
		return this;
	}

	public FakeFilmSource setFilmsInCategory(Map<String, List<String>> filmsInCategory) {
		this.filmsInCategory = filmsInCategory;
		return this;
	}

	@Override
	public void setupIfNotSetup(String variant) {

	}

	public FakeFilmSource setVariants(List<String> variants) {
		this.variants = variants;
		return this;
	}

}
