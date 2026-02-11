package org.downloader.common.utils;

public interface Name {
    default String name() {
        return getClass().getAnnotation(Named.class).value();
    }
}
