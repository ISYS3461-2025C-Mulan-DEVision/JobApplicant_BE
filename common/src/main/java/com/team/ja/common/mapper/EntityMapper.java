package com.team.ja.common.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Enhanced mapper interface for typical REST API patterns.
 * Handles separate Request and Response DTOs for entities.
 *
 * Usage example:
 * 
 * <pre>
 * &#64;Mapper(componentModel = "spring")
 * public interface UserMapper extends EntityMapper&lt;User, CreateUserRequest, UpdateUserRequest, UserResponse&gt; {
 * 
 *     // Override for custom field mappings if needed
 *     &#64;Override
 *     &#64;Mapping(target = "fullName", expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
 *     UserResponse toResponse(User entity);
 * }
 * </pre>
 *
 * @param <E> Entity type
 * @param <C> Create Request DTO type
 * @param <U> Update Request DTO type
 * @param <R> Response DTO type
 */
public interface EntityMapper<E, C, U, R> {

    // ========================================
    // Entity → Response (for API output)
    // ========================================

    /**
     * Convert entity to response DTO
     */
    R toResponse(E entity);

    /**
     * Convert list of entities to list of response DTOs
     */
    List<R> toResponseList(List<E> entities);

    // ========================================
    // Create Request → Entity (for POST)
    // ========================================

    /**
     * Convert create request DTO to new entity
     */
    E fromCreateRequest(C createRequest);

    // ========================================
    // Update Request → Entity (for PUT/PATCH)
    // ========================================

    /**
     * Update existing entity from update request DTO.
     * Null values in the request are ignored (partial update support).
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(U updateRequest, @MappingTarget E entity);
}
