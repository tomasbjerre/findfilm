package findfilm.core.domain;

import static com.google.common.base.Preconditions.checkNotNull;
import findfilm.parser.api.FilmSource;

public class FilmSourceData {
	private String added;
	/**
	 * ID of the film at the {@link FilmSource}.
	 */
	private String filmSourceId;
	/**
	 * ID of the {@link FilmSource}.
	 */
	private String identifier;
	private String lastSeen;
	private String thumbnail;
	private String url;

	public FilmSourceData() {
	}

	public FilmSourceData(final String url, final String thumbnail, final String identifier, final String filmSourceId) {
		this.url = url;
		this.thumbnail = thumbnail;
		this.identifier = identifier;
		this.filmSourceId = filmSourceId;
	}

	public String getAdded() {
		return added;
	}

	public String getFilmSourceId() {
		return this.filmSourceId;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public String getLastSeen() {
		return lastSeen;
	}

	public String getThumbnail() {
		return this.thumbnail;
	}

	public String getUrl() {
		return this.url;
	}

	public FilmSourceData withAdded(String added) {
		this.added = added;
		return this;
	}

	public FilmSourceData withFilmSourceId(final String filmSourceId) {
		this.filmSourceId = checkNotNull(filmSourceId);
		return this;
	}

	public FilmSourceData withIdentifier(final String identifier) {
		this.identifier = checkNotNull(identifier);
		return this;
	}

	public FilmSourceData withLastSeen(String lastSeen) {
		this.lastSeen = lastSeen;
		return this;
	}

	public FilmSourceData withThumbnail(final String thumbnail) {
		this.thumbnail = checkNotNull(thumbnail);
		return this;
	}

	public FilmSourceData withUrl(final String url) {
		this.url = checkNotNull(url);
		return this;
	}
}
