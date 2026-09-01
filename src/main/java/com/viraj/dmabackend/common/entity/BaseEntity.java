package com.viraj.dmabackend.common.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class BaseEntity {

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * ID of the user who created this record.
     * Will be populated automatically later using Spring Security auditing.
     */
    @CreatedBy
    private String createdBy;

    /**
     * ID of the user who last updated this record.
     * Will be populated automatically later using Spring Security auditing.
     */
    @LastModifiedBy
    private String updatedBy;

    @Version
    private Long version;
}
