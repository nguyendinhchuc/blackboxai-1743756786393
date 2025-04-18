package com.ecommerce.api.specification;

import com.ecommerce.api.model.Revision;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class RevisionSpecification extends BaseSpecification<Revision> {

    public RevisionSpecification() {
        super(null);
    }

    /**
     * Create specification for entity name and id
     */
    public static Specification<Revision> byEntityNameAndId(String entityName, Long entityId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (entityName != null) {
                predicates.add(cb.equal(root.get("entityName"), entityName));
            }
            
            if (entityId != null) {
                predicates.add(cb.equal(root.get("entityId"), entityId));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Create specification for username
     */
    public static Specification<Revision> byUsername(String username) {
        return (root, query, cb) -> {
            if (username == null) {
                return null;
            }
            return cb.equal(root.get("username"), username);
        };
    }

    /**
     * Create specification for date range
     */
    public static Specification<Revision> byDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (startDate != null) {
                long startTimestamp = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startTimestamp));
            }
            
            if (endDate != null) {
                long endTimestamp = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endTimestamp));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Create specification for revision type
     */
    public static Specification<Revision> byRevisionType(Revision.RevisionType revisionType) {
        return (root, query, cb) -> {
            if (revisionType == null) {
                return null;
            }
            return cb.equal(root.get("revisionType"), revisionType);
        };
    }

    /**
     * Create specification for IP address
     */
    public static Specification<Revision> byIpAddress(String ipAddress) {
        return (root, query, cb) -> {
            if (ipAddress == null) {
                return null;
            }
            return cb.equal(root.get("ipAddress"), ipAddress);
        };
    }

    /**
     * Create specification for user agent
     */
    public static Specification<Revision> byUserAgent(String userAgent) {
        return (root, query, cb) -> {
            if (userAgent == null) {
                return null;
            }
            return cb.like(root.get("userAgent"), "%" + userAgent + "%");
        };
    }

    /**
     * Create specification for changes containing specific text
     */
    public static Specification<Revision> byChangesContaining(String text) {
        return (root, query, cb) -> {
            if (text == null) {
                return null;
            }
            return cb.like(root.get("changes"), "%" + text + "%");
        };
    }

    /**
     * Create specification for reason containing specific text
     */
    public static Specification<Revision> byReasonContaining(String text) {
        return (root, query, cb) -> {
            if (text == null) {
                return null;
            }
            return cb.like(root.get("reason"), "%" + text + "%");
        };
    }

    /**
     * Create specification for multiple criteria
     */
    public static Specification<Revision> withMultipleCriteria(
            String entityName,
            Long entityId,
            String username,
            Revision.RevisionType revisionType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String ipAddress,
            String userAgent,
            String changesText,
            String reasonText) {
        
        return Specification.where(byEntityNameAndId(entityName, entityId))
                .and(byUsername(username))
                .and(byRevisionType(revisionType))
                .and(byDateRange(startDate, endDate))
                .and(byIpAddress(ipAddress))
                .and(byUserAgent(userAgent))
                .and(byChangesContaining(changesText))
                .and(byReasonContaining(reasonText));
    }

    /**
     * Create specification for sorting
     */
    public static Specification<Revision> withSorting(String sortField, String sortDirection) {
        return (root, query, cb) -> {
            if (sortField != null && sortDirection != null) {
                if (sortDirection.equalsIgnoreCase("ASC")) {
                    query.orderBy(cb.asc(root.get(sortField)));
                } else {
                    query.orderBy(cb.desc(root.get(sortField)));
                }
            }
            return null;
        };
    }

    @Override
    protected Predicate getSearchPredicate(Root<Revision> root, CriteriaBuilder cb, String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        String searchLike = "%" + search.toLowerCase() + "%";
        return cb.or(
            cb.like(cb.lower(root.get("entityName")), searchLike),
            cb.like(cb.lower(root.get("username")), searchLike),
            cb.like(cb.lower(root.get("ipAddress")), searchLike),
            cb.like(cb.lower(root.get("userAgent")), searchLike),
            cb.like(cb.lower(root.get("changes")), searchLike),
            cb.like(cb.lower(root.get("reason")), searchLike)
        );
    }
}
