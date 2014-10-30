package findfilm.core.domain;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

public class Film {
	private String id;
	private final List<FilmSourceData> sources;
	private final String title;

	public Film() {
		this.title = null;
		this.sources = newArrayList();
	}

	public Film(final String title) {
		this.sources = newArrayList();
		this.title = checkNotNull(title);
	}

	public String getId() {
		return this.id;
	}

	public List<FilmSourceData> getSources() {
		return this.sources;
	}

	public String getTitle() {
		return this.title;
	}

	public Film withId(final String id) {
		this.id = id;
		return this;
	}

	public Film withSource(final FilmSourceData source) {
		this.sources.add(source);
		return this;
	}
}
