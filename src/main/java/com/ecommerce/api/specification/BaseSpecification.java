package com.ecommerce.api.specification;

import com.ecommerce.api.model.BaseEntity;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseSpecification<T extends BaseEntity> implements Specification<T> {

    private final transient Map<String, Object> criteria;

    public BaseSpecification(Map<String, Object> criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // Add base predicates for active and non-deleted entities
        predicates.add(cb.equal(root.get("active"), true));
        predicates.add(cb.isNull(root.get("deletedAt")));

        if (criteria != null) {
            criteria.forEach((key, value) -> addPredicate(key, value, root, cb, predicates));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    /**
     * Add predicate based on key and value
     */
    protected void addPredicate(String key, Object value, Root<T> root, CriteriaBuilder cb, List<Predicate> predicates) {
        if (value == null) {
            return;
        }

        switch (key) {
            case "id":
                predicates.add(cb.equal(root.get(key), value));
                break;

            case "ids":
                if (value instanceof List) {
                    predicates.add(root.get("id").in((List<?>) value));
                }
                break;

            case "createdAtStart":
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("createdAt"), (LocalDateTime) value));
                break;

            case "createdAtEnd":
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("createdAt"), (LocalDateTime) value));
                break;

            case "updatedAtStart":
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("updatedAt"), (LocalDateTime) value));
                break;

            case "updatedAtEnd":
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("updatedAt"), (LocalDateTime) value));
                break;

            case "createdBy":
                predicates.add(cb.equal(root.get("createdBy"), value));
                break;

            case "updatedBy":
                predicates.add(cb.equal(root.get("updatedBy"), value));
                break;

            case "search":
                predicates.add(getSearchPredicate(root, cb, value.toString()));
                break;

            default:
                handleCustomField(key, value, root, cb, predicates);
                break;
        }
    }

    /**
     * Get search predicate for string fields
     */
    protected Predicate getSearchPredicate(Root<T> root, CriteriaBuilder cb, String search) {
        return cb.disjunction();
    }

    /**
     * Handle custom field for specific entity
     */
    protected void handleCustomField(String key, Object value, Root<T> root, CriteriaBuilder cb, List<Predicate> predicates) {
        // Override in specific specifications
    }

    /**
     * Create string like predicate
     */
    protected Predicate createStringLikePredicate(Root<T> root, CriteriaBuilder cb, String field, String value) {
        return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }

    /**
     * Create date range predicate
     */
    protected Predicate createDateRangePredicate(Root<T> root, CriteriaBuilder cb, String field,
                                               LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            return cb.between(root.get(field), start, end);
        } else if (start != null) {
            return cb.greaterThanOrEqualTo(root.get(field), start);
        } else if (end != null) {
            return cb.lessThanOrEqualTo(root.get(field), end);
        }
        return null;
    }

    /**
     * Create number range predicate
     */
    protected Predicate createNumberRangePredicate(Root<T> root, CriteriaBuilder cb, String field,
                                                 Number min, Number max) {
        if (min != null && max != null) {
            return cb.between(root.get(field), min, max);
        } else if (min != null) {
            return cb.ge(root.get(field), min);
        } else if (max != null) {
            return cb.le(root.get(field), max);
        }
        return null;
    }

    /**
     * Create boolean predicate
     */
    protected Predicate createBooleanPredicate(Root<T> root, CriteriaBuilder cb, String field, Boolean value) {
        return cb.equal(root.get(field), value);
    }

    /**
     * Create enum predicate
     */
    protected Predicate createEnumPredicate(Root<T> root, CriteriaBuilder cb, String field, Enum<?> value) {
        return cb.equal(root.get(field), value);
    }

    /**
     * Create in predicate
     */
    protected Predicate createInPredicate(Root<T> root, String field, List<?> values) {
        return root.get(field).in(values);
    }

    /**
     * Create join predicate
     */
    protected Predicate createJoinPredicate(Root<T> root, CriteriaBuilder cb, String joinField,
                                          String searchField, Object value) {
        Join<Object, Object> join = root.join(joinField);
        return cb.equal(join.get(searchField), value);
    }

    /**
     * Create or predicate
     */
    protected Predicate createOrPredicate(CriteriaBuilder cb, List<Predicate> predicates) {
        return cb.or(predicates.toArray(new Predicate[0]));
    }

    /**
     * Create and predicate
     */
    protected Predicate createAndPredicate(CriteriaBuilder cb, List<Predicate> predicates) {
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
