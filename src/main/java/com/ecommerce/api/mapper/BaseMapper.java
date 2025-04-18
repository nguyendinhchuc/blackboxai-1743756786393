package com.ecommerce.api.mapper;

import com.ecommerce.api.dto.BaseDTO;
import com.ecommerce.api.model.BaseEntity;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface BaseMapper<E extends BaseEntity, D extends BaseDTO> {

    /**
     * Convert entity to DTO
     */
    D toDto(E entity);

    /**
     * Convert DTO to entity
     */
    E toEntity(D dto);

    /**
     * Update entity from DTO
     */
    void updateEntityFromDto(D dto, @MappingTarget E entity);

    /**
     * Convert list of entities to DTOs
     */
    default List<D> toDtoList(List<E> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of DTOs to entities
     */
    default List<E> toEntityList(List<D> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Convert set of entities to DTOs
     */
    default Set<D> toDtoSet(Set<E> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }

    /**
     * Convert set of DTOs to entities
     */
    default Set<E> toEntitySet(Set<D> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toSet());
    }

    /**
     * Convert page of entities to DTOs
     */
    default Page<D> toDtoPage(Page<E> entityPage) {
        if (entityPage == null) {
            return null;
        }
        List<D> dtos = toDtoList(entityPage.getContent());
        return new PageImpl<>(
            dtos,
            entityPage.getPageable(),
            entityPage.getTotalElements()
        );
    }

    /**
     * Convert page of DTOs to entities
     */
    default Page<E> toEntityPage(Page<D> dtoPage) {
        if (dtoPage == null) {
            return null;
        }
        List<E> entities = toEntityList(dtoPage.getContent());
        return new PageImpl<>(
            entities,
            dtoPage.getPageable(),
            dtoPage.getTotalElements()
        );
    }

    /**
     * Partial update entity from DTO
     */
    default void partialUpdate(D dto, @MappingTarget E entity) {
        if (dto == null || entity == null) {
            return;
        }
        updateEntityFromDto(dto, entity);
    }

    /**
     * Convert entity to map
     */
    default java.util.Map<String, Object> toMap(E entity) {
        if (entity == null) {
            return null;
        }
        return toDto(entity).toMap();
    }

    /**
     * Convert DTO to map
     */
    default java.util.Map<String, Object> toMap(D dto) {
        if (dto == null) {
            return null;
        }
        return dto.toMap();
    }

    /**
     * Convert map to entity
     */
    default E fromMap(java.util.Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        D dto = getDtoClass().cast(BaseDTO.fromMap(map, getDtoClass()));
        return toEntity(dto);
    }

    /**
     * Get entity class
     */
    Class<E> getEntityClass();

    /**
     * Get DTO class
     */
    Class<D> getDtoClass();

    /**
     * Create empty entity
     */
    default E createEntity() {
        try {
            return getEntityClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error creating entity", e);
        }
    }

    /**
     * Create empty DTO
     */
    default D createDto() {
        try {
            return getDtoClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error creating DTO", e);
        }
    }

    /**
     * Copy entity
     */
    default E copy(E entity) {
        if (entity == null) {
            return null;
        }
        return toEntity(toDto(entity));
    }

    /**
     * Copy DTO
     */
    default D copy(D dto) {
        if (dto == null) {
            return null;
        }
        return toDto(toEntity(dto));
    }

    /**
     * Convert to response DTO
     */
    default com.ecommerce.api.payload.ApiResponse<D> toResponse(E entity) {
        if (entity == null) {
            return null;
        }
        return com.ecommerce.api.payload.ApiResponse.success(toDto(entity));
    }

    /**
     * Convert to response DTO list
     */
    default com.ecommerce.api.payload.ApiResponse<List<D>> toResponse(List<E> entities) {
        if (entities == null) {
            return null;
        }
        return com.ecommerce.api.payload.ApiResponse.success(toDtoList(entities));
    }

    /**
     * Convert to response DTO page
     */
    default com.ecommerce.api.payload.ApiResponse<Page<D>> toResponse(Page<E> entityPage) {
        if (entityPage == null) {
            return null;
        }
        return com.ecommerce.api.payload.ApiResponse.success(toDtoPage(entityPage));
    }
}
