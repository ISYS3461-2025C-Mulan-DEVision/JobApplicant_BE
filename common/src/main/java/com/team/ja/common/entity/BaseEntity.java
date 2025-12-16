package com.team.ja.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base entity class that provides common fields for all entities.
 * All domain entities should extend this class.
 * 
 * Features:
 * - UUID primary key
 * - Automatic timestamps (createdAt, updatedAt)
 * - Soft delete support (isActive, deactivatedAt)
 *
 * Usage:
 * 
 * <pre>
 * &#64;Entity
 * &#64;Table(name = "users", schema = "user_schema")
 * &#64;SuperBuilder
 * public class User extends BaseEntity {
 *     private String email;
 *     // other fields...
 * }
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class BaseEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft delete flag. When false, the entity is considered deleted.
     * Default is true (active).
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Timestamp when the entity was deactivated (soft deleted).
     * Null if the entity is still active.
     */
    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    /**
     * Deactivate the entity (soft delete).
     */
    public void deactivate() {
        this.isActive = false;
        this.deactivatedAt = LocalDateTime.now();
    }

    /**
     * Reactivate a previously deactivated entity.
     */
    public void activate() {
        this.isActive = true;
        this.deactivatedAt = null;
    }

    /**
     * Check if the entity is deactivated.
     */
    public boolean isDeactivated() {
        return !isActive;
    }

    /**
     * Indicate whether this entity should be treated as new (insert) by JPA, even
     * if an ID is already present.
     * This is important when IDs are assigned externally (e.g., received via
     * events).
     */
    @Override
    public boolean isNew() {
        // If no createdAt yet, the entity hasn't been persisted
        return createdAt == null;
    }

    /**
     * Ensure an ID is assigned for entities that rely on auto-generated UUIDs.
     * For externally assigned IDs (e.g. via events), the ID will already be set.
     */
    @PrePersist
    protected void assignIdIfNecessary() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}
