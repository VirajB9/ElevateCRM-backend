package com.viraj.dmabackend.menu.mapper;

import com.viraj.dmabackend.menu.dto.MenuResponse;
import com.viraj.dmabackend.menu.dto.MenuTreeResponse;
import com.viraj.dmabackend.menu.entity.Menu;
import org.springframework.stereotype.Component;

@Component
public class MenuMapper {

    public MenuResponse toMenuResponse(Menu menu) {

        if (menu == null) {
            return null;
        }

        return MenuResponse.builder()
                .id(menu.getId())
                .title(menu.getTitle())
                .path(menu.getPath())
                .icon(menu.getIcon())
                .parentId(menu.getParentId())
                .orderIndex(menu.getOrderIndex())
                .requiredPermission(menu.getRequiredPermission())
                .active(menu.getActive())
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .createdBy(menu.getCreatedBy())
                .updatedBy(menu.getUpdatedBy())
                .version(menu.getVersion())
                .build();
    }

    public MenuTreeResponse toMenuTreeResponse(Menu menu) {

        if (menu == null) {
            return null;
        }

        return MenuTreeResponse.builder()
                .id(menu.getId())
                .title(menu.getTitle())
                .path(menu.getPath())
                .icon(menu.getIcon())
                .orderIndex(menu.getOrderIndex())
                .requiredPermission(menu.getRequiredPermission())
                .build();
    }
}
