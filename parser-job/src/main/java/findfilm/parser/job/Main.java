package findfilm.parser.job;

import static com.google.common.collect.Lists.newArrayList;
import static findfilm.storage.StorageFinder.findStorageOfType;
import static java.lang.Thread.currentThread;
import static se.softhouse.jargo.Arguments.stringArgument;
import static se.softhouse.jargo.CommandLineParser.withArguments;

import java.io.IOException;
import java.util.List;

import se.softhouse.jargo.Argument;
import se.softhouse.jargo.ParsedArguments;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import findfilm.parser.api.FilmSource;
import findfilm.parser.job.strategy.SequentialParsingStrategy;
import findfilm.storage.Storage;

public class Main {
	private static List<FilmSource> getFilmSourcesWithCategories(String sourceFilter) throws IOException,
	InstantiationException, IllegalAccessException {
		final List<FilmSource> found = newArrayList();
		final ImmutableSet<ClassInfo> classes = ClassPath.from(currentThread().getContextClassLoader())
				.getTopLevelClassesRecursive("findfilm.parser");
		for (final ClassInfo classInfo : classes) {
			final Class<?> clazz = classInfo.load();
			if (FilmSource.class.isAssignableFrom(clazz) && !clazz.isAssignableFrom(FilmSource.class)) {
				if (sourceFilter.isEmpty() || clazz.getName().contains(sourceFilter)) {
					final FilmSource filmSource = (FilmSource) clazz.newInstance();
					found.add(filmSource);
				}
			}
		}
		return found;
	}

	public static void main(final String[] args) throws Exception {
		final Argument<String> storageType = stringArgument("-storageType").required().build();
		final Argument<String> sourceFilter = stringArgument("-sourceFilter").defaultValue("").build();
		final ParsedArguments arguments = withArguments(storageType).parse(args);
		final Storage storage = findStorageOfType(arguments.get(storageType));
		final List<FilmSource> sourcesWithCategories = getFilmSourcesWithCategories(arguments.get(sourceFilter));
		System.out.println("Found " + sourcesWithCategories.size() + " film sources:");
		for (final FilmSource filmSource : sourcesWithCategories) {
			System.out.println(filmSource.getClass());
		}
		System.out.println("Using storage: " + storage.getIdentifier());
		new SequentialParsingStrategy(sourcesWithCategories, storage);
	}
}
