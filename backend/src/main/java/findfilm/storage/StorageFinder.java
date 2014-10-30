package findfilm.storage;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Thread.currentThread;

import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class StorageFinder {
	public static Storage findStorageOfType(final String type) throws Exception {
		final ImmutableSet<ClassInfo> classes = ClassPath.from(
				currentThread().getContextClassLoader()).getTopLevelClassesRecursive(
						Storage.class.getPackage().getName());
		final List<String> found = newArrayList();
		for (final ClassInfo classInfo : classes) {
			final Class<?> clazz = classInfo.load();
			if (Storage.class.isAssignableFrom(clazz) && !clazz.isAssignableFrom(Storage.class)) {
				final Storage storage = (Storage) clazz.newInstance();
				found.add(storage.getIdentifier());
				if (storage.getIdentifier().equalsIgnoreCase(type)) {
					storage.init();
					return storage;
				}
			}
		}
		throw new RuntimeException("Could not find storage of type \"" + type + "\", found: "
				+ on(", ").join(found));
	}
}
