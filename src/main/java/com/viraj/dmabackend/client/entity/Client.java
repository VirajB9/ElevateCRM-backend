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
public class Client extends BaseEntity {

    @Id
    private String id;

    @TextIndexed
    private String companyName;

    @TextIndexed
    private String contactPerson;

    @TextIndexed
    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String phoneNumber;

    private String website;

    private String industry;

    @Indexed(unique = true, sparse = true)
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
