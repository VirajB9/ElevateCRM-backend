package com.viraj.dmabackend.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.viraj.dmabackend.auth.enums.UserStatus;
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
@Document(collection = "users")
@TypeAlias("User")
@CompoundIndex(name = "idx_role_status", def = "{'roleId': 1, 'status': 1}")
public class User extends BaseEntity {

    @Id
    private String id;

    @TextIndexed
    private String firstName;

    @TextIndexed
    private String lastName;

    @Indexed(unique = true, partialFilter = "{'deleted': false}")
    @TextIndexed
    private String email;

    @Indexed(unique = true, partialFilter = "{'deleted': false}")
    private String phoneNumber;

    /**
     * Bcrypt hashed password.
     * Never store plain text password.
     */
    @JsonIgnore
    private String password;

    private boolean passwordChangeRequired;

    @Indexed
    private String roleId;

    @Builder.Default
    @Indexed
    private UserStatus status = UserStatus.ACTIVE;

    @Builder.Default
    private int failedLoginAttempts = 0;

    private java.time.LocalDateTime lockoutUntil;
}
