package findfilm.storage.impl;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.empty;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import findfilm.core.domain.Film;
import findfilm.core.domain.FilmSourceData;

public class TestIntegrationAPIStorage {
	private RestClient storage;

	@Before
	public void before() {
		this.storage = new RestClient();
	}

	private FilmSourceData correctSource() {
		return new FilmSourceData("url", "thumbnail", "identifier", "filmSourceId");
	}

	private void deleteShouldFail(final String id, final String expectedMessage) {
		try {
			this.storage.delete(id);
			fail("Was expecting exception to be thrown, with message starting with: " + expectedMessage);
		} catch (final Exception e) {
			assertThat(e.getMessage()).startsWith(expectedMessage);
		}
	}

	private void deleteShouldSucceed(final Film film) {
		try {
			this.storage.delete(film.getId());
		} catch (final Exception e) {
			e.printStackTrace();
			fail("Was not expecting failure");
		}
	}

	private void deleteShouldSucceed(final String filmId) {
		final Optional<Film> film = this.storage.get(filmId);
		assertThat(film.isPresent()).as("Film " + filmId + " is not present, cannot try to delete it.").isTrue();
		try {
			this.storage.delete(filmId);
		} catch (final Exception e) {
			e.printStackTrace();
			fail("Was not expecting failure");
		}
	}

	private void getShouldFail(final String id, final String expectedMessage) {
		try {
			this.storage.get(id);
			fail("Was expecting exception to be thrown, with message starting with: " + expectedMessage);
		} catch (final Exception e) {
			assertThat(e.getMessage()).startsWith(expectedMessage);
		}
	}

	private List<Film> getShouldSucceed() {
		try {
			return this.storage.get().get();
		} catch (final Exception e) {
			e.printStackTrace();
			fail("Was not expecting failure");
		}
		return newArrayList();
	}

	private Optional<Film> getShouldSucceed(final String id) {
		try {
			return this.storage.get(id);
		} catch (final Exception e) {
			e.printStackTrace();
			fail("Was not expecting failure");
		}
		return Optional.empty();
	}

	private void postShouldFail(final Film film, final String expectedMessage) {
		try {
			this.storage.post(film);
			fail("Was expecting exception to be thrown, with message starting with: " + expectedMessage);
		} catch (final Exception e) {
			e.printStackTrace();
			assertThat(e.getMessage()).startsWith(expectedMessage);
		}
	}

	private Optional<Film> postShouldSucceed(final Film film, final boolean revert) {
		Optional<Film> storedFilm = Optional.empty();
		try {
			storedFilm = this.storage.post(film);
		} catch (final Exception e) {
			e.printStackTrace();
			fail("Was not expecting failure");
		} finally {
			if (revert) {
				this.deleteShouldSucceed(storedFilm.get());
			}
		}
		return storedFilm;
	}

	private Optional<Film> putShouldSucceed(final Film film) {
		Optional<Film> storedFilm = Optional.empty();
		try {
			storedFilm = this.storage.put(film);
		} catch (final Exception e) {
			e.printStackTrace();
			fail("Was not expecting failure");
		}
		return storedFilm;
	}

	private List<Film> searchShouldSucceed(final Film film) {
		try {
			return this.storage.search(film);
		} catch (final Exception e) {
			propagate(e);
		}
		fail("Was not expecting failure");
		return newArrayList();
	}

	@Test
	public void testDelete() throws Exception {
		final Optional<Film> storedFilm = this.postShouldSucceed(new Film("DUMMY123").withSource(this.correctSource()),
				false);
		this.deleteShouldSucceed(storedFilm.get().getId());
		this.deleteShouldFail(storedFilm.get().getId(), "Unable to find film");
	}

	@Test
	public void testGet() throws Exception {
		Optional<Film> storedFilm1 = this.postShouldSucceed(new Film("DUMMY123").withSource(this.correctSource()),
				false);
		Optional<Film> storedFilm2 = this.postShouldSucceed(new Film("DUMMY1234").withSource(this.correctSource()),
				false);
		try {
			storedFilm1 = this.getShouldSucceed(storedFilm1.get().getId());
			storedFilm2 = this.getShouldSucceed(storedFilm2.get().getId());
			assertThat(getShouldSucceed().size() > 1).isTrue();
		} finally {
			this.deleteShouldSucceed(storedFilm1.get().getId());
			this.deleteShouldSucceed(storedFilm2.get().getId());
			this.getShouldFail(storedFilm1.get().getId(), "Unable to find film");
			this.getShouldFail(storedFilm2.get().getId(), "Unable to find film");
		}
	}

	@Test
	public void testPost() throws Exception {
		this.postShouldFail(new Film("").withSource(this.correctSource()), "Missing title");
		this.postShouldFail(new Film("title123"), "No sources specified");
		this.postShouldFail(new Film("title123").withSource(this.correctSource().withUrl("")), "Missing url");
		this.postShouldFail(new Film(" ").withSource(this.correctSource()), "Missing title");
		this.postShouldFail(new Film("title123").withSource(this.correctSource().withIdentifier(" ")),
				"Missing identifier");
		this.postShouldFail(new Film("title123").withSource(this.correctSource().withUrl(" ")), "Missing url");
		final Optional<Film> storedFilm = this.postShouldSucceed(new Film("DUMMY123").withSource(this.correctSource()),
				false);
		this.postShouldFail(new Film("DUMMY123").withSource(this.correctSource()), "Film exists");
		this.deleteShouldSucceed(storedFilm.get().getId());
		this.postShouldSucceed(new Film("DUMMY123").withSource(this.correctSource().withThumbnail("")), true);
		this.postShouldSucceed(new Film("DUMMY123").withSource(this.correctSource()), true);
		Film posted = this
				.postShouldSucceed(
						new Film("DUMMY123").withSource(this.correctSource().withAdded("2014-01-01")
								.withLastSeen("2014-01-02")), false).get();
		assertThat(posted.getSources().get(0).getAdded()).isEqualTo("2014-01-01");
		assertThat(posted.getSources().get(0).getLastSeen()).isEqualTo("2014-01-02");
		posted = getShouldSucceed().get(0);
		assertThat(posted.getSources().get(0).getAdded()).isEqualTo("2014-01-01");
		assertThat(posted.getSources().get(0).getLastSeen()).isEqualTo("2014-01-02");
		deleteShouldSucceed(posted.getId());
	}

	@Test
	public void testPut() throws Exception {
		Optional<Film> storedFilm = this.postShouldSucceed(new Film("DUMMY123") //
				.withSource(this.correctSource()), false);
		storedFilm = this.putShouldSucceed(storedFilm.get());
		try {
			assertThat(transform(storedFilm.get().getSources(), input -> input.getIdentifier()))//
					.contains("identifier").hasSize(1);
			storedFilm = this.putShouldSucceed(storedFilm.get().withSource(
					this.correctSource().withIdentifier("NetflixUS")));
			assertThat(transform(storedFilm.get().getSources(), input -> input.getIdentifier())) //
					.contains("identifier", "NetflixUS").hasSize(2);
			storedFilm = this.putShouldSucceed(new Film("DUMMY123").withId(storedFilm.get().getId()).withSource(
					this.correctSource().withIdentifier("NetflixUS")));
			assertThat(transform(storedFilm.get().getSources(), input -> input.getIdentifier())) //
					.contains("NetflixUS").hasSize(1);
			storedFilm = this.putShouldSucceed(new Film("DUMMY123").withId(storedFilm.get().getId()) //
					.withSource(correctSource().withAdded("2014-02-01").withLastSeen("2014-02-02")));
			assertThat(storedFilm.get().getSources()).hasSize(1);
			assertThat(storedFilm.get().getSources().get(0).getAdded()).isEqualTo("2014-02-01");
			assertThat(storedFilm.get().getSources().get(0).getLastSeen()).isEqualTo("2014-02-02");
			storedFilm = this.putShouldSucceed(new Film("DUMMY123").withId(storedFilm.get().getId()) //
					.withSource(correctSource().withAdded("2014-03-01").withLastSeen("2014-04-02")));
			assertThat(storedFilm.get().getSources()).hasSize(1);
			assertThat(storedFilm.get().getSources().get(0).getAdded()).isEqualTo("2014-03-01");
			assertThat(storedFilm.get().getSources().get(0).getLastSeen()).isEqualTo("2014-04-02");
			final Film posted = getShouldSucceed().get(0);
			assertThat(posted.getSources().get(0).getAdded()).isEqualTo("2014-03-01");
			assertThat(posted.getSources().get(0).getLastSeen()).isEqualTo("2014-04-02");
		} finally {
			this.deleteShouldSucceed(storedFilm.get().getId());
		}
	}

	@Test
	public void testSearch() throws Exception {
		final Optional<Film> storedFilm = this.postShouldSucceed(new Film("DUMMY123").withSource(this.correctSource()),
				false);
		try {
			List<Film> result = this.searchShouldSucceed(new Film("DUMMY123"));
			assertThat(result).hasSize(1);
			assertThat(result.get(0).getTitle()).isEqualTo("DUMMY123");
			result = this.searchShouldSucceed( //
					new Film() //
							.withSource( //
							new FilmSourceData() //
									.withIdentifier(this.correctSource().getIdentifier()) //
									.withFilmSourceId(this.correctSource().getFilmSourceId())));
			assertThat(result).as("Was unable to search using source identifier and id").hasSize(1);
			assertThat(result.get(0).getTitle()).isEqualTo("DUMMY123");
		} finally {
			this.deleteShouldSucceed(storedFilm.get().getId());
		}
		assertThat(this.searchShouldSucceed(storedFilm.get())).as("Was not expecting search results after delete")
				.isEmpty();
	}

	@Test
	public void testSearchMoreAndPut() {
		Optional<Film> storedFilm = empty();
		try {
			storedFilm = this.postShouldSucceed(new Film("DUMMY123").withSource(this.correctSource()), false);
			assertThat(storage.search( //
					storedFilm.get() //
					)) //
					.hasSize(1);
			assertThat(storage.search( //
					new Film() //
							.withSource( //
							storedFilm.get().getSources().get(0) //
							))) //
					.hasSize(1);
			assertThat(storage.search( //
					new Film(storedFilm.get().getTitle()))) //
					.hasSize(1);
			assertThat(storage.search( //
					new Film().withSource( //
							correctSource().withIdentifier("AnotherIdentifier") //
							))) //
					.hasSize(0);
			assertThat(storage.search( //
					new Film().withSource( //
							correctSource().withFilmSourceId("AnotherfilmSourceId") //
							))) //
					.hasSize(0);
			final Film newSource = new Film(storedFilm.get().getTitle()) //
					.withId(storedFilm.get().getId()) //
					.withSource(storedFilm.get().getSources().get(0)) //
			.withSource( //
					storedFilm.get().getSources().get(0) //
					.withIdentifier("AnotherSource") //
					);
			storage.put(newSource);
			assertThat(storage.search( //
					new Film(storedFilm.get().getTitle()))) //
					.as("Added new source, couldnt find film").hasSize(1);
			assertThat(storage.search( //
					new Film(storedFilm.get().getTitle())).get(0).getSources()) //
					.as("Added new source").hasSize(2);
		} finally {
			this.deleteShouldSucceed(storedFilm.get().getId());
		}
	}
}
