package findfilm.parser.job.strategy;

import static com.google.common.collect.Lists.newArrayList;
import static findfilm.core.FindFilmUtils.setFakeToday;
import static findfilm.core.FindFilmUtils.today;
import static findfilm.parser.job.strategy.TestStorage.incrementId;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import findfilm.core.domain.Film;
import findfilm.core.domain.FilmSourceData;
import findfilm.parser.api.FilmSource;

public class TestSequentialParsingStrategy {

	@Before
	public void before() {
		setFakeToday(empty());
	}

	@Test
	public void testThatExistingFilmAndSourceCanBeExtendedWithLastSeen() throws Exception {
		final TestStorage storage = new TestStorage();
		final TestFilmSource filmSource = new TestFilmSource();
		final List<FilmSource> sourcesWithCategories = newArrayList(filmSource);
		filmSource.setVariants(newArrayList("v1"));
		filmSource.setCategories(newArrayList("c1"));
		filmSource.setFilmsInCategory(ImmutableMap.<String, List<String>> builder() //
				.put("c1", newArrayList("filmSourceId")) //
				.build());
		filmSource.setDetailedFilm(ImmutableMap.<String, Film> builder().put("filmSourceId", new Film("f1").withSource( //
				new FilmSourceData("url", "thumbnail", filmSource.getIdentifier(), "filmSourceId"))).build());
		storage.setFilms(newArrayList());

		setFakeToday(of("2014-01-01"));
		new SequentialParsingStrategy(sourcesWithCategories, storage);
		assertThat(storage.get().get()).hasSize(1);
		assertThat(storage.get().get().get(0).getSources()).hasSize(1);
		assertThat(storage.get().get().get(0).getSources().get(0).getLastSeen()).isEqualTo("2014-01-01");
		assertThat(storage.get().get().get(0).getSources().get(0).getAdded()).isEqualTo("2014-01-01");

		setFakeToday(of("2014-02-01"));
		new SequentialParsingStrategy(sourcesWithCategories, storage);
		assertThat(storage.get().get()).hasSize(1);
		assertThat(storage.get().get().get(0).getSources()).hasSize(1);
		assertThat(storage.get().get().get(0).getSources().get(0).getLastSeen()).isEqualTo("2014-02-01");
		assertThat(storage.get().get().get(0).getSources().get(0).getAdded()).isEqualTo("2014-01-01");
	}

	@Test
	public void testThatExistingFilmCanBeExtendedWithNewSource() throws Exception {
		final TestStorage storage = new TestStorage();
		final TestFilmSource filmSource = new TestFilmSource();
		final List<FilmSource> sourcesWithCategories = newArrayList(filmSource);

		filmSource.setVariants(newArrayList("v1"));
		filmSource.setCategories(newArrayList("c1"));
		filmSource.setFilmsInCategory(ImmutableMap.<String, List<String>> builder() //
				.put("c1", newArrayList("filmSourceId")) //
				.build());
		filmSource.setDetailedFilm(ImmutableMap.<String, Film> builder().put("filmSourceId", new Film("f1").withSource( //
				new FilmSourceData("url", "thumbnail", filmSource.getIdentifier(), "filmSourceId"))).build());

		storage.setFilms(newArrayList(new Film("f1").withId(incrementId() + "").withSource(
				new FilmSourceData("url", "thumbnail", "existingFilmSourceIdentifier", "existingFilmSourceId")
						.withAdded("2014-01-01").withLastSeen("2014-01-01"))));
		new SequentialParsingStrategy(sourcesWithCategories, storage);

		assertThat(storage.get().get()).hasSize(1);
		assertThat(storage.get().get().get(0).getSources()).hasSize(2);
		assertThat(storage.get().get().get(0).getSources().get(0).getAdded()).isEqualTo("2014-01-01");
		assertThat(storage.get().get().get(0).getSources().get(0).getLastSeen()).isEqualTo("2014-01-01");
		assertThat(storage.get().get().get(0).getSources().get(0).getFilmSourceId()).isEqualTo("existingFilmSourceId");
		assertThat(storage.get().get().get(0).getSources().get(0).getFilmSourceId()).isEqualTo("existingFilmSourceId");
		assertThat(storage.get().get().get(0).getSources().get(1).getAdded()).isEqualTo(today());
		assertThat(storage.get().get().get(0).getSources().get(1).getLastSeen()).isEqualTo(today());
		assertThat(storage.get().get().get(0).getSources().get(1).getIdentifier()).isEqualTo("TestIdentifier");
		assertThat(storage.get().get().get(0).getSources().get(1).getFilmSourceId()).isEqualTo("filmSourceId");
	}

	@Test
	public void testThatNewFilmCanBeAdded() throws Exception {
		final TestStorage storage = new TestStorage();
		final TestFilmSource filmSource = new TestFilmSource();
		final List<FilmSource> sourcesWithCategories = newArrayList(filmSource);
		filmSource.setVariants(newArrayList("v1"));
		filmSource.setCategories(newArrayList("c1"));
		filmSource.setFilmsInCategory(ImmutableMap.<String, List<String>> builder() //
				.put("c1", newArrayList("f1")) //
				.build());
		filmSource.setDetailedFilm(ImmutableMap.<String, Film> builder().put("f1", new Film("f1").withSource( //
				new FilmSourceData("url", "thumbnail", filmSource.getIdentifier(), "filmSourceId"))).build());
		storage.setFilms(newArrayList());
		new SequentialParsingStrategy(sourcesWithCategories, storage);

		assertThat(storage.get().get()).hasSize(1);
		assertThat(storage.get().get().get(0).getTitle()).isEqualTo("f1");
		assertThat(storage.get().get().get(0).getSources()).hasSize(1);
		assertThat(storage.get().get().get(0).getSources().get(0).getAdded()).isEqualTo(today());
		assertThat(storage.get().get().get(0).getSources().get(0).getLastSeen()).isEqualTo(today());
		assertThat(storage.get().get().get(0).getSources().get(0).getIdentifier())
				.isEqualTo(filmSource.getIdentifier());
		assertThat(storage.get().get().get(0).getSources().get(0).getFilmSourceId()).isEqualTo("filmSourceId");
	}
}
