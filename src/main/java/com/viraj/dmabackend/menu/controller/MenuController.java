package com.viraj.dmabackend.menu.controller;

import com.viraj.dmabackend.menu.dto.CreateMenuRequest;
import com.viraj.dmabackend.menu.dto.MenuResponse;
import com.viraj.dmabackend.menu.dto.MenuTreeResponse;
import com.viraj.dmabackend.menu.dto.UpdateMenuRequest;
import com.viraj.dmabackend.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "8. Dynamic Menu Management")
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class MenuController {

    private final MenuService menuService;

    @PreAuthorize("hasAuthority('menu:create')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Menu")
    public MenuResponse createMenu(
            @Valid @RequestBody CreateMenuRequest request) {

         return menuService.createMenu(request);
    }

    @PreAuthorize("hasAuthority('menu:read')")
    @GetMapping("/{menuId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Menu By Id")
    public MenuResponse getMenuById(
            @PathVariable String menuId) {

        return menuService.getMenuById(menuId);
    }

    @PreAuthorize("hasAuthority('menu:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get All Menus")
    public List<MenuResponse> getAllMenus() {

        return menuService.getAllMenus();
    }

    @PreAuthorize("hasAuthority('menu:update')")
    @PutMapping("/{menuId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Menu")
    public MenuResponse updateMenu(
            @PathVariable String menuId,
            @Valid @RequestBody UpdateMenuRequest request){

        return menuService.updateMenu(menuId, request);
    }

    @PreAuthorize("hasAuthority('menu:delete')")
    @PatchMapping("/{menuId}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Deactivate Menu")
    public void deactivateMenu(
            @PathVariable String menuId) {

        menuService.deactivateMenu(menuId);
    }

    @PreAuthorize("hasAuthority('menu:read')")
    @GetMapping("/tree")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Menu Tree")
    public List<MenuTreeResponse> getMenuTree() {

        return menuService.getMenuTree();
    }
}
