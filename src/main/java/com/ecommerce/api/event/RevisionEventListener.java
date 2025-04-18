package com.ecommerce.api.event;

import com.ecommerce.api.service.RevisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevisionEventListener {

    private final RevisionService revisionService;

    /**
     * Handle revision event after transaction commit
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRevisionEvent(RevisionEvent event) {
        try {
            revisionService.createRevision(
                event.getEntityName(),
                event.getEntityId(),
                event.getRevisionType(),
                event.getChanges(),
                event.getReason(),
                event.getRequest()
            );
            log.info("Created revision for {} with ID {}", event.getEntityName(), event.getEntityId());
        } catch (Exception e) {
            log.error("Error creating revision for {} with ID {}: {}", 
                event.getEntityName(), event.getEntityId(), e.getMessage(), e);
        }
    }

    /**
     * Handle revision event immediately
     */
    @EventListener
    public void handleImmediateRevisionEvent(ImmediateRevisionEvent event) {
        try {
            revisionService.createRevision(
                event.getEntityName(),
                event.getEntityId(),
                event.getRevisionType(),
                event.getChanges(),
                event.getReason(),
                event.getRequest()
            );
            log.info("Created immediate revision for {} with ID {}", 
                event.getEntityName(), event.getEntityId());
        } catch (Exception e) {
            log.error("Error creating immediate revision for {} with ID {}: {}", 
                event.getEntityName(), event.getEntityId(), e.getMessage(), e);
        }
    }

    /**
     * Handle revision event after rollback
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleRevisionEventAfterRollback(RevisionEvent event) {
        log.warn("Transaction rolled back for {} with ID {}", event.getEntityName(), event.getEntityId());
    }

    /**
     * Handle revision event before commit
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleRevisionEventBeforeCommit(RevisionEvent event) {
        log.debug("Processing revision before commit for {} with ID {}", 
            event.getEntityName(), event.getEntityId());
    }

    /**
     * Handle revision event after completion
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleRevisionEventAfterCompletion(RevisionEvent event) {
        log.debug("Completed revision processing for {} with ID {}", 
            event.getEntityName(), event.getEntityId());
    }

    /**
     * Immediate revision event class
     */
    public static class ImmediateRevisionEvent extends RevisionEvent {
        public ImmediateRevisionEvent(RevisionEvent event) {
            super(event.getSource(), event.getEntityName(), event.getEntityId(), 
                  event.getRevisionType(), event.getChanges(), event.getReason(), 
                  event.getRequest(), event.getUsername(), event.getEntity(), 
                  event.getOldEntity());
        }
    }

    /**
     * Handle revision event with custom error handling
     */
    private void handleRevisionEventWithErrorHandling(RevisionEvent event, String context) {
        try {
            revisionService.createRevision(
                event.getEntityName(),
                event.getEntityId(),
                event.getRevisionType(),
                event.getChanges(),
                event.getReason(),
                event.getRequest()
            );
            log.info("[{}] Created revision for {} with ID {}", 
                context, event.getEntityName(), event.getEntityId());
        } catch (Exception e) {
            log.error("[{}] Error creating revision for {} with ID {}: {}", 
                context, event.getEntityName(), event.getEntityId(), e.getMessage(), e);
            handleRevisionError(event, e);
        }
    }

    /**
     * Handle revision error
     */
    private void handleRevisionError(RevisionEvent event, Exception error) {
        // Here you could implement retry logic, error notification, or other error handling
        log.error("Failed to process revision event: {}", error.getMessage());
        
        // You might want to store failed events for later retry
        // failedEventRepository.save(new FailedRevisionEvent(event, error));
        
        // Or notify administrators
        // notificationService.notifyAdmins("Revision Error", createErrorMessage(event, error));
    }

    /**
     * Create error message
     */
    private String createErrorMessage(RevisionEvent event, Exception error) {
        return String.format(
            "Failed to create revision for %s (ID: %d)\nError: %s\nStack trace: %s",
            event.getEntityName(),
            event.getEntityId(),
            error.getMessage(),
            org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(error)
        );
    }
}
