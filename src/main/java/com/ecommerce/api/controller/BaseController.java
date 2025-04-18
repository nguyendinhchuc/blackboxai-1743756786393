package com.ecommerce.api.controller;

import com.ecommerce.api.model.BaseEntity;
import com.ecommerce.api.payload.ApiResponse;
import com.ecommerce.api.service.BaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

public abstract class BaseController<T extends BaseEntity> {

    protected final BaseService<T> service;

    protected BaseController(BaseService<T> service) {
        this.service = service;
    }

    /**
     * Get all entities
     */
    @GetMapping
    @Operation(summary = "Get all entities", description = "Returns a list of all entities")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden")
    })
    public ResponseEntity<ApiResponse<List<T>>> getAll() {
        return ApiResponse.ok("Successfully retrieved all entities", service.findAll());
    }

    /**
     * Get all entities with pagination
     */
    @GetMapping("/page")
    @Operation(summary = "Get all entities with pagination", description = "Returns a page of entities")
    public ResponseEntity<ApiResponse<Page<T>>> getAllPaginated(
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok("Successfully retrieved paginated entities", service.findAll(pageable));
    }

    /**
     * Get entity by id
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get entity by id", description = "Returns a single entity")
    public ResponseEntity<ApiResponse<T>> getById(
            @Parameter(description = "ID of entity to be obtained", required = true)
            @PathVariable Long id) {
        return ApiResponse.ok("Successfully retrieved entity", service.findByIdOrThrow(id));
    }

    /**
     * Create entity
     */
    @PostMapping
    @Operation(summary = "Create new entity", description = "Creates a new entity")
    public ResponseEntity<ApiResponse<T>> create(@Valid @RequestBody T entity) {
        return ApiResponse.ok("Successfully created entity", service.save(entity));
    }

    /**
     * Update entity
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update entity", description = "Updates an existing entity")
    public ResponseEntity<ApiResponse<T>> update(
            @Parameter(description = "ID of entity to be updated", required = true)
            @PathVariable Long id,
            @Valid @RequestBody T entity) {
        return ApiResponse.ok("Successfully updated entity", service.update(id, entity));
    }

    /**
     * Delete entity
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete entity", description = "Deletes an entity")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID of entity to be deleted", required = true)
            @PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok("Successfully deleted entity", null);
    }

    /**
     * Restore entity
     */
    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore entity", description = "Restores a deleted entity")
    public ResponseEntity<ApiResponse<T>> restore(
            @Parameter(description = "ID of entity to be restored", required = true)
            @PathVariable Long id) {
        return ApiResponse.ok("Successfully restored entity", service.restore(id));
    }

    /**
     * Get all deleted entities
     */
    @GetMapping("/deleted")
    @Operation(summary = "Get all deleted entities", description = "Returns a list of all deleted entities")
    public ResponseEntity<ApiResponse<List<T>>> getAllDeleted() {
        return ApiResponse.ok("Successfully retrieved deleted entities", service.findAllDeleted());
    }

    /**
     * Get all deleted entities with pagination
     */
    @GetMapping("/deleted/page")
    @Operation(summary = "Get all deleted entities with pagination", description = "Returns a page of deleted entities")
    public ResponseEntity<ApiResponse<Page<T>>> getAllDeletedPaginated(
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok("Successfully retrieved paginated deleted entities", service.findAllDeleted(pageable));
    }

    /**
     * Batch create entities
     */
    @PostMapping("/batch")
    @Operation(summary = "Create multiple entities", description = "Creates multiple entities in a single request")
    public ResponseEntity<ApiResponse<List<T>>> batchCreate(@Valid @RequestBody List<T> entities) {
        return ApiResponse.ok("Successfully created entities", service.saveAll(entities));
    }

    /**
     * Batch delete entities
     */
    @DeleteMapping("/batch")
    @Operation(summary = "Delete multiple entities", description = "Deletes multiple entities in a single request")
    public ResponseEntity<ApiResponse<Void>> batchDelete(@RequestBody List<Long> ids) {
        service.deleteAll(ids);
        return ApiResponse.ok("Successfully deleted entities", null);
    }

    /**
     * Update active status
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update entity status", description = "Updates the active status of an entity")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @Parameter(description = "ID of entity to be updated", required = true)
            @PathVariable Long id,
            @Parameter(description = "New active status", required = true)
            @RequestParam boolean active) {
        service.updateActiveStatus(id, active);
        return ApiResponse.ok("Successfully updated entity status", null);
    }

    /**
     * Count entities
     */
    @GetMapping("/count")
    @Operation(summary = "Count entities", description = "Returns the total count of entities")
    public ResponseEntity<ApiResponse<Long>> count() {
        return ApiResponse.ok("Successfully counted entities", service.count());
    }

    /**
     * Check if entity exists
     */
    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if entity exists", description = "Returns whether an entity exists")
    public ResponseEntity<ApiResponse<Boolean>> exists(
            @Parameter(description = "ID of entity to check", required = true)
            @PathVariable Long id) {
        return ApiResponse.ok("Successfully checked entity existence", service.existsById(id));
    }
}
