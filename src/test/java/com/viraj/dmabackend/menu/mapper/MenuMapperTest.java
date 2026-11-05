package com.viraj.dmabackend.menu.mapper;

import com.viraj.dmabackend.menu.dto.MenuResponse;
import com.viraj.dmabackend.menu.dto.MenuTreeResponse;
import com.viraj.dmabackend.menu.entity.Menu;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MenuMapperTest {

    private final MenuMapper menuMapper = new MenuMapper();

    @Test
    void toMenuResponse_ReturnsNullWhenMenuIsNull() {
        assertNull(menuMapper.toMenuResponse(null));
    }

    @Test
    void toMenuTreeResponse_ReturnsNullWhenMenuIsNull() {
        assertNull(menuMapper.toMenuTreeResponse(null));
    }

    @Test
    void toMenuResponse_MapsAllFieldsCorrectly() {
        Menu menu = Menu.builder()
                .id("m-1")
                .title("Settings")
                .path("/settings")
                .icon("cog")
                .parentId("p-1")
                .orderIndex(2)
                .requiredPermission("settings:read")
                .active(true)
                .build();

        MenuResponse response = menuMapper.toMenuResponse(menu);

        assertNotNull(response);
        assertEquals("m-1", response.getId());
        assertEquals("Settings", response.getTitle());
        assertEquals("/settings", response.getPath());
        assertEquals("cog", response.getIcon());
        assertEquals("p-1", response.getParentId());
        assertEquals(2, response.getOrderIndex());
        assertEquals("settings:read", response.getRequiredPermission());
        assertTrue(response.getActive());
    }

    @Test
    void toMenuTreeResponse_MapsFieldsCorrectly() {
        Menu menu = Menu.builder()
                .id("m-2")
                .title("Users")
                .path("/users")
                .icon("user")
                .orderIndex(1)
                .requiredPermission("user:read")
                .build();

        MenuTreeResponse response = menuMapper.toMenuTreeResponse(menu);

        assertNotNull(response);
        assertEquals("m-2", response.getId());
        assertEquals("Users", response.getTitle());
        assertEquals("/users", response.getPath());
        assertEquals("user", response.getIcon());
        assertEquals(1, response.getOrderIndex());
        assertEquals("user:read", response.getRequiredPermission());
    }
}
