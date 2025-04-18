package com.ecommerce.api.service;

import com.ecommerce.api.dto.RevisionDTO;
import com.ecommerce.api.exception.CustomException;
import com.ecommerce.api.model.Revision;
import com.ecommerce.api.repository.RevisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevisionService {

    private final RevisionRepository revisionRepository;

    /**
     * Create a new revision
     */
    @Transactional
    public RevisionDTO createRevision(String entityName, Long entityId, 
                                    Revision.RevisionType revisionType,
                                    Map<String, Object> changes, 
                                    String reason,
                                    HttpServletRequest request) {
        Revision revision = new Revision();
        revision.setEntityName(entityName);
        revision.setEntityId(entityId);
        revision.setRevisionType(revisionType);
        revision.setChangesFromMap(changes);
        revision.setReason(reason);
        revision.setTimestamp(System.currentTimeMillis());
        
        // Set request information
        if (request != null) {
            revision.setIpAddress(getClientIp(request));
            revision.setUserAgent(request.getHeader("User-Agent"));
        }

        return revisionRepository.save(revision).toDTO();
    }

    /**
     * Get revision by id
     */
    @Transactional(readOnly = true)
    public RevisionDTO getRevision(Long id) {
        return revisionRepository.findById(id)
            .map(Revision::toDTO)
            .orElseThrow(() -> new CustomException.ResourceNotFoundException("Revision", "id", id));
    }

    /**
     * Get revisions by entity
     */
    @Transactional(readOnly = true)
    public List<RevisionDTO> getRevisionsByEntity(String entityName, Long entityId) {
        return revisionRepository.findByEntityNameAndEntityId(entityName, entityId)
            .stream()
            .map(Revision::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get revisions by entity with pagination
     */
    @Transactional(readOnly = true)
    public Page<RevisionDTO> getRevisionsByEntity(String entityName, Long entityId, Pageable pageable) {
        return revisionRepository.findByEntityNameAndEntityId(entityName, entityId, pageable)
            .map(Revision::toDTO);
    }

    /**
     * Get revisions by username
     */
    @Transactional(readOnly = true)
    public List<RevisionDTO> getRevisionsByUsername(String username) {
        return revisionRepository.findByUsername(username)
            .stream()
            .map(Revision::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get revisions by date range
     */
    @Transactional(readOnly = true)
    public List<RevisionDTO> getRevisionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        long startTimestamp = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTimestamp = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        return revisionRepository.findByDateRange(startTimestamp, endTimestamp)
            .stream()
            .map(Revision::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get revisions by type
     */
    @Transactional(readOnly = true)
    public List<RevisionDTO> getRevisionsByType(Revision.RevisionType revisionType) {
        return revisionRepository.findByRevisionType(revisionType)
            .stream()
            .map(Revision::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get latest revision for entity
     */
    @Transactional(readOnly = true)
    public RevisionDTO getLatestRevision(String entityName, Long entityId) {
        Revision revision = revisionRepository.findLatestRevision(entityName, entityId);
        if (revision == null) {
            throw new CustomException.ResourceNotFoundException(
                String.format("No revisions found for %s with id %d", entityName, entityId));
        }
        return revision.toDTO();
    }

    /**
     * Search revisions by multiple criteria
     */
    @Transactional(readOnly = true)
    public Page<RevisionDTO> searchRevisions(
            String entityName,
            Long entityId,
            String username,
            Revision.RevisionType revisionType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        
        Long startTimestamp = startDate != null ? 
            startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null;
        Long endTimestamp = endDate != null ? 
            endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null;

        return revisionRepository.findByMultipleCriteria(
            entityName, entityId, username, revisionType, startTimestamp, endTimestamp, pageable
        ).map(Revision::toDTO);
    }

    /**
     * Clean up old revisions
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional
    public void cleanupOldRevisions() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(6); // Keep 6 months of history
        long cutoffTimestamp = cutoffDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        revisionRepository.deleteOldRevisions(cutoffTimestamp);
    }

    /**
     * Get client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    /**
     * Count revisions by entity
     */
    @Transactional(readOnly = true)
    public long countRevisionsByEntity(String entityName, Long entityId) {
        return revisionRepository.countByEntityNameAndEntityId(entityName, entityId);
    }

    /**
     * Count revisions by username
     */
    @Transactional(readOnly = true)
    public long countRevisionsByUsername(String username) {
        return revisionRepository.countByUsername(username);
    }

    /**
     * Count revisions by type
     */
    @Transactional(readOnly = true)
    public long countRevisionsByType(Revision.RevisionType revisionType) {
        return revisionRepository.countByRevisionType(revisionType);
    }

    /**
     * Get revisions by IP address
     */
    @Transactional(readOnly = true)
    public List<RevisionDTO> getRevisionsByIpAddress(String ipAddress) {
        return revisionRepository.findByIpAddress(ipAddress)
            .stream()
            .map(Revision::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get revisions by user agent
     */
    @Transactional(readOnly = true)
    public List<RevisionDTO> getRevisionsByUserAgent(String userAgent) {
        return revisionRepository.findByUserAgent(userAgent)
            .stream()
            .map(Revision::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get revisions containing specific changes
     */
    @Transactional(readOnly = true)
    public List<RevisionDTO> getRevisionsByChanges(String changePattern) {
        return revisionRepository.findByChanges(changePattern)
            .stream()
            .map(Revision::toDTO)
            .collect(Collectors.toList());
    }
}
