package com.ecommerce.api.mapper;

import com.ecommerce.api.dto.RevisionDTO;
import com.ecommerce.api.model.Revision;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RevisionMapper extends BaseMapper<Revision, RevisionDTO> {

    @Override
    @Mapping(target = "changes", ignore = true)
    RevisionDTO toDto(Revision entity);

    @Override
    @Mapping(target = "changes", ignore = true)
    Revision toEntity(RevisionDTO dto);

    @Override
    @Mapping(target = "changes", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(RevisionDTO dto, @MappingTarget Revision entity);

    /**
     * Custom mapping for changes
     */
    @AfterMapping
    default void mapChanges(@MappingTarget RevisionDTO dto, Revision entity) {
        if (entity != null) {
            dto.setChanges(entity.getChangesAsMap());
        }
    }

    @AfterMapping
    default void mapChanges(@MappingTarget Revision entity, RevisionDTO dto) {
        if (dto != null) {
            entity.setChangesFromMap(dto.getChanges());
        }
    }

    /**
     * Convert timestamp to date
     */
    default java.util.Date timestampToDate(long timestamp) {
        return new java.util.Date(timestamp);
    }

    /**
     * Convert date to timestamp
     */
    default long dateToTimestamp(java.util.Date date) {
        return date != null ? date.getTime() : 0;
    }

    /**
     * Get entity class
     */
    @Override
    default Class<Revision> getEntityClass() {
        return Revision.class;
    }

    /**
     * Get DTO class
     */
    @Override
    default Class<RevisionDTO> getDtoClass() {
        return RevisionDTO.class;
    }

    /**
     * Create revision DTO builder
     */
    @Named("dtoBuilder")
    default RevisionDTO.RevisionDTOBuilder createDtoBuilder() {
        return RevisionDTO.builder();
    }

    /**
     * Map revision type
     */
    default String mapRevisionType(Revision.RevisionType revisionType) {
        return revisionType != null ? revisionType.name() : null;
    }

    /**
     * Map revision type from string
     */
    default Revision.RevisionType mapRevisionTypeFromString(String revisionType) {
        return revisionType != null ? Revision.RevisionType.valueOf(revisionType) : null;
    }

    /**
     * Map entity with null check
     */
    @Named("safeMap")
    default RevisionDTO safeMap(Revision entity) {
        return entity != null ? toDto(entity) : null;
    }

    /**
     * Map DTO with null check
     */
    @Named("safeMapDto")
    default Revision safeMapDto(RevisionDTO dto) {
        return dto != null ? toEntity(dto) : null;
    }

    /**
     * Map page of entities
     */
    @IterableMapping(qualifiedByName = "safeMap")
    org.springframework.data.domain.Page<RevisionDTO> mapPage(
        org.springframework.data.domain.Page<Revision> page);

    /**
     * Map list of entities
     */
    @IterableMapping(qualifiedByName = "safeMap")
    java.util.List<RevisionDTO> mapList(java.util.List<Revision> entities);

    /**
     * Map set of entities
     */
    @IterableMapping(qualifiedByName = "safeMap")
    java.util.Set<RevisionDTO> mapSet(java.util.Set<Revision> entities);

    /**
     * Map list of DTOs
     */
    @IterableMapping(qualifiedByName = "safeMapDto")
    java.util.List<Revision> mapDtoList(java.util.List<RevisionDTO> dtos);

    /**
     * Map set of DTOs
     */
    @IterableMapping(qualifiedByName = "safeMapDto")
    java.util.Set<Revision> mapDtoSet(java.util.Set<RevisionDTO> dtos);
}
