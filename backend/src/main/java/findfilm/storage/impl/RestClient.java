package findfilm.storage.impl;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static findfilm.core.Logger.DEBUG;
import static findfilm.core.Logger.log;
import static findfilm.core.PropertyLoader.getStoredProperty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import findfilm.core.domain.Film;
import findfilm.storage.Storage;

public class RestClient implements Storage {
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final String RESOURCE_FILMS = "/films.php";
	private static final String RESOURCE_SEARCH = "/search.php";

	public RestClient() {
	}

	private HttpURLConnection createConnection(final String request) throws IOException {
		final URL url = new URL(request);
		HttpURLConnection connection;
		connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		return connection;
	}

	@Override
	public void delete(final String filmId) {
		this.doRequest(this.getBaseUrl(RESOURCE_FILMS), "DELETE", Optional.of(filmId), new TypeToken<Film>() {
		});
	}

	public <T> Optional<T> doRequest(String request, final String method, final Optional<String> data,
			final TypeToken<T> expectedResponseType) {
		HttpURLConnection connection = null;
		try {
			if (newArrayList("POST", "PUT").contains(method) && data.isPresent()) {
				connection = this.createConnection(request);
				connection.setRequestMethod(method);
				log(method + "> " + request, DEBUG);
				log("> > > >\n" + data.get(), DEBUG);
				final OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
				out.write(data.get());
				out.close();
			} else {
				if (data.isPresent()) {
					request += "&id=" + data.get();
				}
				log(method + "> " + request, DEBUG);
				connection = this.createConnection(request);
				connection.setRequestMethod(method);
			}
			try {
				final String response = this.getResponse(connection.getInputStream());
				return Optional.of(gson.fromJson(response, expectedResponseType.getType()));
			} catch (final IOException e) {
				e.printStackTrace();
				final String response = this.getResponse(connection.getErrorStream());
				@SuppressWarnings("unchecked")
				final Map<Object, Object> responseMap = gson.fromJson(response, Map.class);
				throw new RuntimeException("" + responseMap.get("message"), e);
			}
		} catch (final Exception e) {
			propagate(e);
		} finally {
			connection.disconnect();
		}
		return Optional.empty();
	}

	@Override
	public Optional<List<Film>> get() {
		return this.doRequest(this.getBaseUrl(RESOURCE_FILMS), "GET", Optional.empty(), new TypeToken<List<Film>>() {
		});
	}

	@Override
	public Optional<Film> get(final String filmId) {
		return this.doRequest(this.getBaseUrl(RESOURCE_FILMS), "GET", Optional.of(filmId), new TypeToken<Film>() {
		});
	}

	private String getBaseUrl(final String resource) {
		final String request = getStoredProperty("api_film_base");
		final String secretKey = getStoredProperty("api_film_secretkey");
		return request + resource + "?secretkey=" + secretKey;
	}

	@Override
	public String getIdentifier() {
		return "API";
	}

	private String getResponse(final InputStream stream) throws IOException {
		log("< < < <", DEBUG);
		final String response = CharStreams.toString(new InputStreamReader(stream, UTF_8));
		log(response, DEBUG);
		return response;
	}

	@Override
	public void init() throws Exception {
	}

	@Override
	public Optional<Film> post(final Film film) {
		return this.doRequest(this.getBaseUrl(RESOURCE_FILMS), "POST", Optional.of(gson.toJson(film)),
				new TypeToken<Film>() {
				});
	}

	@Override
	public Optional<Film> put(final Film film) {
		return this.doRequest(this.getBaseUrl(RESOURCE_FILMS), "PUT", Optional.of(gson.toJson(film)),
				new TypeToken<Film>() {
				});
	}

	@Override
	public List<Film> search(final Film film) {
		return this.doRequest(this.getBaseUrl(RESOURCE_SEARCH), "POST", Optional.of(gson.toJson(film)),
				new TypeToken<List<Film>>() {
				}).get();
	}
}
