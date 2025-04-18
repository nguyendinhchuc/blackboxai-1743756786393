package com.ecommerce.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseDTO implements Serializable {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    private Boolean active;

    private Long version;

    /**
     * Convert entity to DTO
     */
    public static <D extends BaseDTO, E> D fromEntity(E entity, Class<D> dtoClass) {
        if (entity == null) {
            return null;
        }
        try {
            D dto = dtoClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Error converting entity to DTO", e);
        }
    }

    /**
     * Convert DTO to entity
     */
    public static <D extends BaseDTO, E> E toEntity(D dto, Class<E> entityClass) {
        if (dto == null) {
            return null;
        }
        try {
            E entity = entityClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(dto, entity);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Error converting DTO to entity", e);
        }
    }

    /**
     * Convert list of entities to DTOs
     */
    public static <D extends BaseDTO, E> List<D> fromEntities(List<E> entities, Class<D> dtoClass) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(entity -> fromEntity(entity, dtoClass))
                .collect(Collectors.toList());
    }

    /**
     * Convert list of DTOs to entities
     */
    public static <D extends BaseDTO, E> List<E> toEntities(List<D> dtos, Class<E> entityClass) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(dto -> toEntity(dto, entityClass))
                .collect(Collectors.toList());
    }

    /**
     * Convert page of entities to DTOs
     */
    public static <D extends BaseDTO, E> org.springframework.data.domain.Page<D> fromEntitiesPage(
            org.springframework.data.domain.Page<E> page, Class<D> dtoClass) {
        return page.map(entity -> fromEntity(entity, dtoClass));
    }

    /**
     * Update entity from DTO
     */
    public static <D extends BaseDTO, E> void updateEntity(D dto, E entity) {
        if (dto == null || entity == null) {
            return;
        }
        BeanUtils.copyProperties(dto, entity, getNullPropertyNames(dto));
    }

    /**
     * Get null property names
     */
    @JsonIgnore
    private static String[] getNullPropertyNames(Object source) {
        final org.springframework.beans.BeanWrapper wrappedSource = 
            new org.springframework.beans.BeanWrapperImpl(source);
        java.util.Set<String> nullPropertyNames = new java.util.HashSet<>();
        for (java.beans.PropertyDescriptor pd : wrappedSource.getPropertyDescriptors()) {
            if (wrappedSource.getPropertyValue(pd.getName()) == null) {
                nullPropertyNames.add(pd.getName());
            }
        }
        return nullPropertyNames.toArray(new String[0]);
    }

    /**
     * Convert to map
     */
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", id);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("createdBy", createdBy);
        map.put("updatedBy", updatedBy);
        map.put("active", active);
        map.put("version", version);
        return map;
    }

    /**
     * Create from map
     */
    public static <D extends BaseDTO> D fromMap(java.util.Map<String, Object> map, Class<D> dtoClass) {
        try {
            D dto = dtoClass.getDeclaredConstructor().newInstance();
            if (map.containsKey("id")) {
                dto.setId(Long.valueOf(map.get("id").toString()));
            }
            if (map.containsKey("createdAt")) {
                dto.setCreatedAt(LocalDateTime.parse(map.get("createdAt").toString()));
            }
            if (map.containsKey("updatedAt")) {
                dto.setUpdatedAt(LocalDateTime.parse(map.get("updatedAt").toString()));
            }
            if (map.containsKey("createdBy")) {
                dto.setCreatedBy(map.get("createdBy").toString());
            }
            if (map.containsKey("updatedBy")) {
                dto.setUpdatedBy(map.get("updatedBy").toString());
            }
            if (map.containsKey("active")) {
                dto.setActive(Boolean.valueOf(map.get("active").toString()));
            }
            if (map.containsKey("version")) {
                dto.setVersion(Long.valueOf(map.get("version").toString()));
            }
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Error creating DTO from map", e);
        }
    }

    /**
     * Create builder
     */
    public static <D extends BaseDTO> org.springframework.data.domain.PageRequest createPageRequest(
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection) {
        org.springframework.data.domain.Sort.Direction direction = 
            org.springframework.data.domain.Sort.Direction.fromString(
                sortDirection != null ? sortDirection : "DESC"
            );
        return org.springframework.data.domain.PageRequest.of(
            page != null ? page : 0,
            size != null ? size : 10,
            org.springframework.data.domain.Sort.by(direction, sortBy != null ? sortBy : "id")
        );
    }
}
