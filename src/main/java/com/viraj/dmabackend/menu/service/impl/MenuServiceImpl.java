package com.viraj.dmabackend.menu.service.impl;

import com.viraj.dmabackend.menu.dto.CreateMenuRequest;
import com.viraj.dmabackend.menu.dto.MenuResponse;
import com.viraj.dmabackend.menu.dto.MenuTreeResponse;
import com.viraj.dmabackend.menu.dto.UpdateMenuRequest;
import com.viraj.dmabackend.menu.entity.Menu;
import com.viraj.dmabackend.menu.exception.MenuNotFoundException;
import com.viraj.dmabackend.menu.mapper.MenuMapper;
import com.viraj.dmabackend.menu.repository.MenuRepository;
import com.viraj.dmabackend.menu.service.MenuService;
import com.viraj.dmabackend.menu.validator.MenuValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;

    private final MenuMapper menuMapper;

    private final MenuValidator menuValidator;

    @Override
    public MenuResponse createMenu(CreateMenuRequest request) {

        validateCreateMenuRequest(request);

        Menu menu = buildMenu(request);

        Menu savedMenu = menuRepository.save(menu);

        return menuMapper.toMenuResponse(savedMenu);
    }

    @Override
    public MenuResponse getMenuById(String menuId) {

        Menu menu = findMenuById(menuId);

        return menuMapper.toMenuResponse(menu);
    }

    @Override
    public List<MenuResponse> getAllMenus() {

        List<Menu> menus = menuRepository.findAll(
                Sort.by(Sort.Direction.ASC, "orderIndex"));

        return mapToMenuResponseList(menus);
    }

    @Override
    public MenuResponse updateMenu(String menuId, UpdateMenuRequest request) {

        Menu menu = findMenuById(menuId);

        validateUpdateMenuRequest(menuId, request);

        updateMenuFields(menu, request);

        Menu updatedMenu = menuRepository.save(menu);

        return menuMapper.toMenuResponse(updatedMenu);
    }

    @Override
    public void deactivateMenu(String menuId) {

        Menu menu = findMenuById(menuId);

        menu.setActive(false);

        menuRepository.save(menu);
    }

    @Override
    public List<MenuTreeResponse> getMenuTree() {

        List<Menu> menus = menuRepository.findByActiveTrueOrderByOrderIndexAsc();

        menus = filterMenusByPermission(menus);

        return buildMenuTree(menus);
    }


    // =========================
    // Helper Methods
    // =========================
    private Menu findMenuById(String menuId) {

        return menuRepository.findById(menuId)
                .orElseThrow(() ->
                        new MenuNotFoundException(menuId));
    }

    private void validateCreateMenuRequest(CreateMenuRequest request) {

        validateCommonMenuFields(request.getPath(), request.getParentId());

        menuValidator.validateDuplicateTitle(request.getTitle());

        menuValidator.validateDuplicatePath(request.getPath());

    }

    private void validateUpdateMenuRequest(String menuId, UpdateMenuRequest request) {

        validateCommonMenuFields(request.getPath(), request.getParentId());

        menuValidator.validateDuplicateTitleForUpdate(request.getTitle(), menuId);

        menuValidator.validateDuplicatePathForUpdate(request.getPath(), menuId);

        menuValidator.validateParentRelationship(menuId, request.getParentId());
    }

    private void validateCommonMenuFields(String path, String parentId) {

        menuValidator.validatePath(path);

        menuValidator.validateParentExists(parentId);
    }

    private List<Menu> filterMenusByPermission(List<Menu> menus) {

        Set<String> authorities = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return menus.stream()
                .filter(menu ->
                        menu.getRequiredPermission() == null
                                || menu.getRequiredPermission().isBlank()
                                || authorities.contains(menu.getRequiredPermission()))
                .toList();
    }

    private Menu buildMenu(CreateMenuRequest request) {

        return Menu.builder()
                .title(request.getTitle().trim())
                .path(request.getPath().trim())
                .icon(request.getIcon() != null ? request.getIcon().trim() : null)
                .parentId(request.getParentId())
                .orderIndex(request.getOrderIndex())
                .requiredPermission(request.getRequiredPermission() != null
                        ? request.getRequiredPermission().trim()
                        : null)
                .active(true)
                .build();
    }

    private void updateMenuFields(Menu menu, UpdateMenuRequest request) {

        menu.setTitle(request.getTitle().trim());
        menu.setPath(request.getPath().trim());
        menu.setIcon(request.getIcon() != null ? request.getIcon().trim() : null);
        menu.setParentId(request.getParentId());
        menu.setOrderIndex(request.getOrderIndex());
        menu.setRequiredPermission(request.getRequiredPermission() != null
                ? request.getRequiredPermission().trim()
                : null);
        menu.setActive(request.getActive());

    }

    private List<MenuTreeResponse> buildMenuTree(List<Menu> menus) {

        Map<String, MenuTreeResponse> menuMap = new HashMap<>();

        List<MenuTreeResponse> rootMenus = new ArrayList<>();

        // Convert every Menu into MenuTreeResponse
        for (Menu menu : menus) {
            menuMap.put(
                    menu.getId(),
                    menuMapper.toMenuTreeResponse(menu));
        }

        // Build the hierarchy
        for (Menu menu : menus) {

            MenuTreeResponse currentMenu = menuMap.get(menu.getId());

            if (menu.getParentId() == null || menu.getParentId().isBlank()) {
                rootMenus.add(currentMenu);
            } else {

                MenuTreeResponse parentMenu = menuMap.get(menu.getParentId());

                if (parentMenu != null) {
                    parentMenu.getChildren().add(currentMenu);
                }
            }
        }
        return rootMenus;
    }

    private List<MenuResponse> mapToMenuResponseList(List<Menu> menus) {

        return menus.stream()
                .map(menuMapper::toMenuResponse)
                .toList();
    }
}
