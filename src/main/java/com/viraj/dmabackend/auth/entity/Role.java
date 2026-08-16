package com.viraj.dmabackend.auth.entity;

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

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "roles")
@TypeAlias("Role")
public class Role extends BaseEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    @Builder.Default
    private List<String> permissionIds = new ArrayList<>();

    @Builder.Default
    private boolean systemRole = false;
}
