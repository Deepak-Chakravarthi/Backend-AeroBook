package com.aerobook.util.Jpa;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SpecificationBuilder<T> {

    private final List<Specification<T>> specs = new ArrayList<>();

    public static <T> SpecificationBuilder<T> builder() {
        return new SpecificationBuilder<>();
    }

    public SpecificationBuilder<T> addEquals(String field, Object value) {
        if (isPresent(value)) {
            specs.add((root, query, cb) ->
                    cb.equal(root.get(field), value));
        }
        return this;
    }

    public SpecificationBuilder<T> addLike(String field, String value) {
        if (isPresent(value)) {
            specs.add((root, query, cb) ->
                    cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%"));
        }
        return this;
    }


    public SpecificationBuilder<T> addJoinEquals(String joinField, String field, Object value) {
        if (isPresent(value)) {
            specs.add((root, query, cb) ->
                    cb.equal(root.join(joinField, JoinType.INNER).get(field), value));
        }
        return this;
    }


    public <E extends Enum<E>> SpecificationBuilder<T> addEnumEquals(
            String field, String value, Class<E> enumClass) {
        if (isPresent(value)) {
            try {
                E parsed = Enum.valueOf(enumClass, value.toUpperCase());
                specs.add((root, query, cb) ->
                        cb.equal(root.get(field), parsed));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid value '" + value + "' for " + enumClass.getSimpleName()
                                + ". Valid values: " + enumValuesAsString(enumClass));
            }
        }
        return this;
    }


    public Specification<T> build() {
        if (specs.isEmpty()) {
            return Specification.where((root, query, cb) -> null);
        }
        return specs.stream()
                .reduce((spec1, spec2) -> spec1.and(spec2))
                .orElse(Specification.where((root, query, cb) -> null));
    }

    private boolean isPresent(Object value) {
        if (value == null) return false;
        if (value instanceof String s) return !s.isBlank();
        return true;
    }

    private <E extends Enum<E>> String enumValuesAsString(Class<E> enumClass) {
        StringBuilder sb = new StringBuilder();
        for (E constant : enumClass.getEnumConstants()) {
            sb.append(constant.name()).append(", ");
        }
        return sb.toString().replaceAll(", $", "");
    }
}