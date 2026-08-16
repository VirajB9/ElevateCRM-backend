package com.viraj.dmabackend.auth.entity;

import com.viraj.dmabackend.auth.enums.PermissionType;
import com.viraj.dmabackend.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "permissions")
@TypeAlias("Permission")
public class Permission extends BaseEntity {
    @Id
    private String id;

    private PermissionType permissionType;

    private String module;

    private String description;
}
