package org.downloader.common.exceptions;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class ConversionSourceNotFoundException extends NoSuchFileException {
    public ConversionSourceNotFoundException(String message) {
        super(message);
    }

    public ConversionSourceNotFoundException(Path path) {
        super("Source file not found: %s".formatted(path));
    }
}
