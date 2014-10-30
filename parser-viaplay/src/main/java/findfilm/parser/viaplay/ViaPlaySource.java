package findfilm.parser.viaplay;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.empty;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;

import findfilm.core.domain.Film;
import findfilm.core.domain.FilmSourceData;
import findfilm.parser.api.FilmSource;
import findfilm.storage.impl.RestClient;

public class ViaPlaySource implements FilmSource {

	private final Map<String, List<String>> categoryFilmsMap = newHashMap();
	private final RestClient client = new RestClient();

	@SuppressWarnings("rawtypes")
	private final TypeToken expectedMap = new TypeToken<Map>() {
	};
	private final Map<String, Film> filmDetailsMap = newHashMap();

	@Override
	public List<String> getCategories() {
		final List<String> categories = newArrayList();
		categories.add("serier");
		categories.add("film");
		return categories;
	}

	private String getCategory(final String string) {
		return "https://content.viaplay.se/pc-se/" + string + "/samtliga?sort=recently_added";
	}

	@Override
	public Film getDetailedFilm(final String id) {
		return filmDetailsMap.get(id);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Film> getFilmDetails(final String category) {
		final List<Film> toReturn = newArrayList();
		List<Film> newFound = null;
		for (int page = 1; newFound == null || !newFound.isEmpty(); page++) {
			newFound = newArrayList();
			final Optional<Map> resp = client.doRequest(
					getCategory(category) + "&block=1&partial=1&pageNumber=" + page, "GET", empty(), expectedMap);
			final Map embedded2 = (Map) resp.get().get("_embedded");
			final List<Map> products = (List<Map>) embedded2.get("viaplay:products");

			for (final Map p : products) {
				final String publicPath = (String) p.get("publicPath");
				final Map content = (Map) p.get("content");
				final Map imgs = (Map) content.get("images");
				final Map boxArt = (Map) imgs.get("boxart");
				String title = (String) content.get("title");
				if (content.containsKey("series")) {
					final Map series = (Map) content.get("series");
					title = (String) series.get("title");
				}
				newFound.add( //
				new Film(title) //
						.withSource( //
						new FilmSourceData() //
								.withFilmSourceId(publicPath) //
								.withIdentifier(getIdentifier()) //
								.withUrl("http://viaplay.se/" + category + "/" + publicPath) //
								.withThumbnail(boxArt == null ? null : (String) boxArt.get("url"))));
			}
			toReturn.addAll(newFound);
		}
		return toReturn;
	}

	@Override
	public List<String> getFilmsInCategory(final String category) {
		if (categoryFilmsMap.containsKey(category)) {
			return categoryFilmsMap.get(category);
		}
		final List<String> filmsInCategory = newArrayList();
		final List<Film> films = getFilmDetails(category);
		for (final Film f : films) {
			final String id = f.getSources().get(0).getFilmSourceId();
			filmsInCategory.add(id);
			filmDetailsMap.put(id, f);
		}
		categoryFilmsMap.put(category, filmsInCategory);
		return newArrayList(filmsInCategory);
	}

	@Override
	public String getIdentifier() {
		return "ViaPlay";
	}

	@Override
	public List<String> getVariants() {
		return newArrayList("SE");
	}

	@Override
	public void quit() {

	}

	@Override
	public void setupIfNotSetup(String variant) {
	}
}
