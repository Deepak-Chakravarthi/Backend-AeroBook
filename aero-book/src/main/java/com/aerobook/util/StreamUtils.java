package com.aerobook.util;

import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.stream.Stream;

@NoArgsConstructor
public class StreamUtils {

    public static long countNonNull(Object... values) {
        return Stream.of(values)
                .filter(Objects::nonNull)
                .count();
    }
}
