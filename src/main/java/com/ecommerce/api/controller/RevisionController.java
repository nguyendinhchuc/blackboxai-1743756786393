package com.ecommerce.api.controller;

import com.ecommerce.api.dto.RevisionDTO;
import com.ecommerce.api.model.Revision;
import com.ecommerce.api.payload.ApiResponse;
import com.ecommerce.api.service.RevisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/revisions")
@RequiredArgsConstructor
@Tag(name = "Revision Controller", description = "Endpoints for managing entity revisions")
@SecurityRequirement(name = "bearerAuth")
public class RevisionController {

    private final RevisionService revisionService;

    /**
     * Get revision by id
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get revision by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RevisionDTO>> getRevision(
            @Parameter(description = "Revision ID", required = true)
            @PathVariable Long id) {
        return ApiResponse.ok("Revision retrieved successfully", revisionService.getRevision(id));
    }

    /**
     * Get revisions by entity
     */
    @GetMapping("/entity/{entityName}/{entityId}")
    @Operation(summary = "Get revisions by entity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RevisionDTO>>> getRevisionsByEntity(
            @Parameter(description = "Entity name", required = true)
            @PathVariable String entityName,
            @Parameter(description = "Entity ID", required = true)
            @PathVariable Long entityId) {
        return ApiResponse.ok(
            "Revisions retrieved successfully",
            revisionService.getRevisionsByEntity(entityName, entityId)
        );
    }

    /**
     * Get revisions by entity with pagination
     */
    @GetMapping("/entity/{entityName}/{entityId}/page")
    @Operation(summary = "Get paginated revisions by entity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RevisionDTO>>> getRevisionsByEntityPaginated(
            @Parameter(description = "Entity name", required = true)
            @PathVariable String entityName,
            @Parameter(description = "Entity ID", required = true)
            @PathVariable Long entityId,
            @Parameter(description = "Pagination information")
            Pageable pageable) {
        return ApiResponse.ok(
            "Revisions retrieved successfully",
            revisionService.getRevisionsByEntity(entityName, entityId, pageable)
        );
    }

    /**
     * Get revisions by username
     */
    @GetMapping("/user/{username}")
    @Operation(summary = "Get revisions by username")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RevisionDTO>>> getRevisionsByUsername(
            @Parameter(description = "Username", required = true)
            @PathVariable String username) {
        return ApiResponse.ok(
            "Revisions retrieved successfully",
            revisionService.getRevisionsByUsername(username)
        );
    }

    /**
     * Get revisions by date range
     */
    @GetMapping("/date-range")
    @Operation(summary = "Get revisions by date range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RevisionDTO>>> getRevisionsByDateRange(
            @Parameter(description = "Start date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.ok(
            "Revisions retrieved successfully",
            revisionService.getRevisionsByDateRange(startDate, endDate)
        );
    }

    /**
     * Get revisions by type
     */
    @GetMapping("/type/{revisionType}")
    @Operation(summary = "Get revisions by type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RevisionDTO>>> getRevisionsByType(
            @Parameter(description = "Revision type", required = true)
            @PathVariable Revision.RevisionType revisionType) {
        return ApiResponse.ok(
            "Revisions retrieved successfully",
            revisionService.getRevisionsByType(revisionType)
        );
    }

    /**
     * Create revision
     */
    @PostMapping("/entity/{entityName}/{entityId}")
    @Operation(summary = "Create new revision")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RevisionDTO>> createRevision(
            @Parameter(description = "Entity name", required = true)
            @PathVariable String entityName,
            @Parameter(description = "Entity ID", required = true)
            @PathVariable Long entityId,
            @Parameter(description = "Revision type", required = true)
            @RequestParam Revision.RevisionType revisionType,
            @Parameter(description = "Changes", required = true)
            @RequestBody Map<String, Object> changes,
            @Parameter(description = "Reason for revision")
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        return ApiResponse.ok(
            "Revision created successfully",
            revisionService.createRevision(entityName, entityId, revisionType, changes, reason, request)
        );
    }

    /**
     * Search revisions
     */
    @GetMapping("/search")
    @Operation(summary = "Search revisions by multiple criteria")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RevisionDTO>>> searchRevisions(
            @Parameter(description = "Entity name")
            @RequestParam(required = false) String entityName,
            @Parameter(description = "Entity ID")
            @RequestParam(required = false) Long entityId,
            @Parameter(description = "Username")
            @RequestParam(required = false) String username,
            @Parameter(description = "Revision type")
            @RequestParam(required = false) Revision.RevisionType revisionType,
            @Parameter(description = "Start date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Pagination information")
            Pageable pageable) {
        return ApiResponse.ok(
            "Revisions retrieved successfully",
            revisionService.searchRevisions(entityName, entityId, username, revisionType, startDate, endDate, pageable)
        );
    }

    /**
     * Get latest revision for entity
     */
    @GetMapping("/entity/{entityName}/{entityId}/latest")
    @Operation(summary = "Get latest revision for entity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RevisionDTO>> getLatestRevision(
            @Parameter(description = "Entity name", required = true)
            @PathVariable String entityName,
            @Parameter(description = "Entity ID", required = true)
            @PathVariable Long entityId) {
        return ApiResponse.ok(
            "Latest revision retrieved successfully",
            revisionService.getLatestRevision(entityName, entityId)
        );
    }

    /**
     * Get revision counts
     */
    @GetMapping("/counts")
    @Operation(summary = "Get revision counts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getRevisionCounts(
            @Parameter(description = "Entity name")
            @RequestParam(required = false) String entityName,
            @Parameter(description = "Entity ID")
            @RequestParam(required = false) Long entityId,
            @Parameter(description = "Username")
            @RequestParam(required = false) String username,
            @Parameter(description = "Revision type")
            @RequestParam(required = false) Revision.RevisionType revisionType) {
        Map<String, Long> counts = new java.util.HashMap<>();
        
        if (entityName != null && entityId != null) {
            counts.put("entityCount", revisionService.countRevisionsByEntity(entityName, entityId));
        }
        if (username != null) {
            counts.put("userCount", revisionService.countRevisionsByUsername(username));
        }
        if (revisionType != null) {
            counts.put("typeCount", revisionService.countRevisionsByType(revisionType));
        }

        return ApiResponse.ok("Revision counts retrieved successfully", counts);
    }
}
