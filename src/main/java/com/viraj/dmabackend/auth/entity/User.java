package com.viraj.dmabackend.auth.entity;

import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "users")
@TypeAlias("User")
public class User extends BaseEntity {

    @Id
    private String id;

    private String firstName;

    private String lastName;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String phoneNumber;

    /**
     * Bcrypt hashed password.
     * Never store plain text password.
     */
    private String password;

    private String roleId;

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
}

