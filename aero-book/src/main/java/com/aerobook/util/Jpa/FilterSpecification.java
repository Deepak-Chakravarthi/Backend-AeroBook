package com.aerobook.util.Jpa;

import com.aerobook.util.Jpa.domain.Action;
import com.aerobook.util.Jpa.domain.OperationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Generated;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FilterSpecification<T,V extends FilterSpecification<T,V>> implements Specification<T> {

    protected final List<Specification<T>> specifications =new ArrayList<>();

    protected final List<Predicate> customPredicates= new ArrayList<>();


    public static <T, V extends FilterSpecification<T,V>> FilterSpecification<T,V> of(@NonNull Class<T> currentClass){
        return new FilterSpecification();
    }

    public V addCriteria(@NonNull String field, @NonNull OperationType operation, Object... objects){
        return this.addCriteria(Action.DEFAULT,field, operation,objects);
    }

    private V addCriteria(Action action, @NonNull String field, @NonNull OperationType operation, Object[] objects) {

    }

//    public V addCriteria(@NonNull Action action,@NonNull String field,@NonNull OperationType operationType,Object... objects ){
//        if(this.isIgnored(action,objects)){
//            return this;
//        } else if (objects.length==1) {
//            this.addCriteria(FilterSpecificationFactory.of(field,operationType,objects[0]));
//            return this;
//        } else if (objects.length == 2) {
//            this.addCriteria(FilterSpecificationFactory.of(field,operationType,objects[0],objects[1]));
//            return this;
//        }
//        else {
//            throw new ImplementationError("Amount of values not supported:" + objects.length);
//        }
//    }

    private boolean isIgnored(@NonNull Action action, Object[] objects) {
        return action == Action.IGNORE_NULL_VALUE && Arrays.stream(objects).anyMatch(Objects::nonNull);
    }


    public V addCriterias(@NonNull List<Specification<T>> specifications){
        this.specifications.addAll(specifications);
        return (V) this;
    }

    @Override
    public @Nullable Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates= this.specifications.stream().map((specifications)->{
            return specifications.toPredicate(root, query, criteriaBuilder);
        }).toList();
        this.customPredicates.addAll(predicates);
        return criteriaBuilder.and((Predicate[]) this.customPredicates.toArray(new Predicate[0]));
    }

    @Generated
    protected List<Specification<T>> getSpecifications(){
        return this.specifications;
    }

    @Generated
    protected List<Predicate> getCustomPredicates(){
        return this.customPredicates;
    }

}
