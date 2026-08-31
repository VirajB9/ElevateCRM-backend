package com.viraj.dmabackend.menu.entity;

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
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "menus")
@TypeAlias("Menu")
@CompoundIndex(
        name = "idx_active_order_index",
        def = "{'active': 1, 'orderIndex': 1}")
public class Menu extends BaseEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String title;

    @Indexed(unique = true)
    private String path;

    private String icon;

    @Indexed
    private String parentId;

    private Integer orderIndex;

    @Indexed
    private String requiredPermission;

    @Builder.Default
    private Boolean active = true;
}
