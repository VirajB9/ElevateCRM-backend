package com.viraj.dmabackend.lead.entity;

import com.viraj.dmabackend.common.entity.BaseEntity;
import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "leads")
@TypeAlias("Lead")
public class
Lead extends BaseEntity {

    @Id
    private String id;

    private String firstName;

    private String lastName;

    private String companyName;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String phoneNumber;

    private String website;

    private String industry;

    private LeadSource source;

    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    /**
     * User responsible for handling this lead.
     * Stores User ID only (No DBRef).
     */
    private String assignedUserId;

    private BigDecimal estimatedBudget;

    private String requirements;

    private String notes;

    /**
     * Populated after successful lead conversion.
     */
    private String convertedClientId;

    /**
     * Timestamp when the lead was converted.
     */
    private LocalDateTime convertedAt;
}

