package com.viraj.dmabackend.menu.controller;

import com.viraj.dmabackend.menu.dto.CreateMenuRequest;
import com.viraj.dmabackend.menu.dto.MenuResponse;
import com.viraj.dmabackend.menu.dto.MenuTreeResponse;
import com.viraj.dmabackend.menu.dto.UpdateMenuRequest;
import com.viraj.dmabackend.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "8. Dynamic Menu Management", description = "Endpoints for managing dynamic sidebar menus")
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class MenuController {

    private final MenuService menuService;

    @PreAuthorize("hasAuthority('menu:create')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Menu", description = "Creates a new menu item. Requires 'menu:create' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Menu item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public MenuResponse createMenu(
            @Valid @RequestBody CreateMenuRequest request) {
         return menuService.createMenu(request);
    }

    @PreAuthorize("hasAuthority('menu:read')")
    @GetMapping("/{menuId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Menu By Id", description = "Retrieves a specific menu item by its ID. Requires 'menu:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu item retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Menu item not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public MenuResponse getMenuById(
            @Parameter(description = "Unique ID of the menu item", required = true)
            @PathVariable String menuId) {
        return menuService.getMenuById(menuId);
    }

    @PreAuthorize("hasAuthority('menu:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get All Menus", description = "Retrieves a flat list of all active menu items. Requires 'menu:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu items retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public List<MenuResponse> getAllMenus() {
        return menuService.getAllMenus();
    }

    @PreAuthorize("hasAuthority('menu:update')")
    @PutMapping("/{menuId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Menu", description = "Updates an existing menu item. Requires 'menu:update' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Menu item not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public MenuResponse updateMenu(
            @Parameter(description = "Unique ID of the menu item", required = true)
            @PathVariable String menuId,
            @Valid @RequestBody UpdateMenuRequest request){
        return menuService.updateMenu(menuId, request);
    }

    @PreAuthorize("hasAuthority('menu:delete')")
    @PatchMapping("/{menuId}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Deactivate Menu", description = "Soft deletes a menu item by setting it to inactive. Requires 'menu:delete' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu item deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Menu item not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public void deactivateMenu(
            @Parameter(description = "Unique ID of the menu item", required = true)
            @PathVariable String menuId) {
        menuService.deactivateMenu(menuId);
    }

    @PreAuthorize("hasAuthority('menu:read')")
    @GetMapping("/tree")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Menu Tree", description = "Retrieves the menu hierarchy formatted as a tree, filtered by user permissions. Requires 'menu:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu tree retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public List<MenuTreeResponse> getMenuTree() {
        return menuService.getMenuTree();
    }
}
