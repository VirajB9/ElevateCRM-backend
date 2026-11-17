package com.viraj.dmabackend.client.entity;

import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "clients")
@TypeAlias("Client")
@CompoundIndex(name = "idx_client_status_deleted", def = "{'status': 1, 'deleted': 1}")
@CompoundIndex(name = "idx_client_deleted_created_at", def = "{'deleted': 1, 'createdAt': -1}")
public class Client extends BaseEntity {

    @Id
    private String id;

    @TextIndexed
    @Indexed(unique = true, partialFilter = "{'deleted': false, 'companyName': {'$type': 'string'}}")
    private String companyName;

    @TextIndexed
    private String contactPerson;

    @TextIndexed
    @Indexed(unique = true, partialFilter = "{'deleted': false}")
    private String email;

    @Indexed(unique = true, partialFilter = "{'deleted': false, 'phoneNumber': {'$type': 'string'}}")
    private String phoneNumber;

    private String website;

    private String industry;

    @Indexed(unique = true, partialFilter = "{'deleted': false, 'gstNumber': {'$type': 'string'}}")
    private String gstNumber;

    private String address;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private String notes;

    @Builder.Default
    @Indexed
    private ClientStatus status = ClientStatus.ACTIVE;
}
