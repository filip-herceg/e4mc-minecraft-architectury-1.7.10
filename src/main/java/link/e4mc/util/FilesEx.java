package link.e4mc.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Simple NIO file utilities with better error signaling than java.io.File APIs.
 * Java 8 compatible.
 */
public final class FilesEx {
    private static final Logger LOGGER = LogManager.getLogger(FilesEx.class);

    private FilesEx() {
        throw new AssertionError("No instances");
    }

    public static void delete(Path path) throws IOException {
        try {
            Files.delete(path);
        } catch (NoSuchFileException e) {
            // Re-throw: explicit missing file is an important signal to callers
            throw e;
        } catch (IOException e) {
            LOGGER.warn("Failed to delete {}: {}", path, e.toString());
            throw e;
        }
    }

    public static boolean deleteIfExists(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.warn("Failed to deleteIfExists {}: {}", path, e.toString());
            return false;
        }
    }

    public static void moveReplace(Path source, Path target) throws IOException {
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
