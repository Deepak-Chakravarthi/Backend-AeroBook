package com.aerobook.util.stream;

import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * The type Stream utils.
 */
@NoArgsConstructor
public class StreamUtils {

    /**
     * Count non null long.
     *
     * @param values the values
     * @return the long
     */
    public static long countNonNull(Object... values) {
        return Stream.of(values)
                .filter(Objects::nonNull)
                .count();
    }
}
