package com.team.ja.common.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Base mapper interface with common mapping methods.
 * All service-specific mappers should extend this interface.
 *
 * Usage example:
 * <pre>
 * @Mapper(componentModel = "spring")
 * public interface UserMapper extends BaseMapper<User, UserDTO> {
 *     // Additional custom mappings if needed
 * }
 * </pre>
 *
 * @param <E> Entity type
 * @param <D> DTO type
 */
public interface BaseMapper<E, D> {

    /**
     * Convert entity to DTO
     */
    D toDto(E entity);

    /**
     * Convert DTO to entity
     */
    E toEntity(D dto);

    /**
     * Convert list of entities to list of DTOs
     */
    List<D> toDtoList(List<E> entities);

    /**
     * Convert list of DTOs to list of entities
     */
    List<E> toEntityList(List<D> dtos);

    /**
     * Update entity from DTO (ignores null values)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(D dto, @MappingTarget E entity);
}

