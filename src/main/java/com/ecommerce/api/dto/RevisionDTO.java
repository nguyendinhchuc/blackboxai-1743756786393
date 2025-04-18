package com.ecommerce.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RevisionDTO {

    private Long id;
    
    private long timestamp;
    
    private String username;
    
    private String ipAddress;
    
    private String userAgent;
    
    private com.ecommerce.api.model.Revision.RevisionType revisionType;
    
    private String entityName;
    
    private Long entityId;
    
    private Map<String, Object> changes;
    
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getRevisionDate() {
        return new Date(timestamp);
    }

    public String getFormattedRevisionDate() {
        return java.text.DateFormat.getDateTimeInstance().format(getRevisionDate());
    }

    /**
     * Convert to entity
     */
    public com.ecommerce.api.model.Revision toEntity() {
        return com.ecommerce.api.model.Revision.fromDTO(this);
    }

    /**
     * Create from entity
     */
    public static RevisionDTO fromEntity(com.ecommerce.api.model.Revision entity) {
        return entity.toDTO();
    }

    /**
     * Convert to map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", id);
        map.put("timestamp", timestamp);
        map.put("username", username);
        map.put("ipAddress", ipAddress);
        map.put("userAgent", userAgent);
        map.put("revisionType", revisionType);
        map.put("entityName", entityName);
        map.put("entityId", entityId);
        map.put("changes", changes);
        map.put("reason", reason);
        return map;
    }

    /**
     * Create from map
     */
    public static RevisionDTO fromMap(Map<String, Object> map) {
        return RevisionDTO.builder()
                .id(map.get("id") != null ? Long.valueOf(map.get("id").toString()) : null)
                .timestamp(map.get("timestamp") != null ? Long.parseLong(map.get("timestamp").toString()) : 0)
                .username((String) map.get("username"))
                .ipAddress((String) map.get("ipAddress"))
                .userAgent((String) map.get("userAgent"))
                .revisionType(map.get("revisionType") != null ? 
                    com.ecommerce.api.model.Revision.RevisionType.valueOf(map.get("revisionType").toString()) : null)
                .entityName((String) map.get("entityName"))
                .entityId(map.get("entityId") != null ? Long.valueOf(map.get("entityId").toString()) : null)
                .changes((Map<String, Object>) map.get("changes"))
                .reason((String) map.get("reason"))
                .build();
    }
}
