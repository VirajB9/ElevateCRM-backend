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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;

    private final MenuMapper menuMapper;

    private final MenuValidator menuValidator;

    @Override
    @CacheEvict(value = "menuTree", allEntries = true)
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

        if (menus.size() > 500) {
            log.warn("Menu count exceeds expected threshold: {}", menus.size());
        }

        return mapToMenuResponseList(menus);
    }

    @Override
    @CacheEvict(value = "menuTree", allEntries = true)
    public MenuResponse updateMenu(String menuId, UpdateMenuRequest request) {

        Menu menu = findMenuById(menuId);

        if (request.getVersion() != null && !request.getVersion().equals(menu.getVersion())) {
            throw new org.springframework.dao.OptimisticLockingFailureException(
                    "Menu has been modified by another operation. Please refresh and try again."
            );
        }

        validateUpdateMenuRequest(menuId, request);

        if (Boolean.FALSE.equals(request.getActive())) {
            menuValidator.validateMenuDeactivation(menuId);
        }

        updateMenuFields(menu, request);

        Menu updatedMenu = menuRepository.save(menu);

        return menuMapper.toMenuResponse(updatedMenu);
    }

    @Override
    @CacheEvict(value = "menuTree", allEntries = true)
    public void deactivateMenu(String menuId) {

        Menu menu = findMenuById(menuId);

        menuValidator.validateMenuDeactivation(menuId);

        menu.setActive(false);

        menuRepository.save(menu);
    }

    @Cacheable(value = "menuTree", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication() != null ? T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getAuthorities().toString() : 'anonymous'")
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

        String trimmedMenuId = menuId != null ? menuId.trim() : null;
        return menuRepository.findById(trimmedMenuId)
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

        org.springframework.security.core.Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Set<String> authorities = (authentication != null && authentication.getAuthorities() != null)
                ? authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet())
                : Collections.emptySet();

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
                .parentId(normalizeParentId(request.getParentId()))
                .orderIndex(request.getOrderIndex())
                .requiredPermission(request.getRequiredPermission() != null && !request.getRequiredPermission().isBlank()
                        ? request.getRequiredPermission().trim()
                        : null)
                .active(true)
                .build();
    }

    private void updateMenuFields(Menu menu, UpdateMenuRequest request) {

        menu.setTitle(request.getTitle().trim());
        menu.setPath(request.getPath().trim());
        menu.setIcon(request.getIcon() != null ? request.getIcon().trim() : null);
        menu.setParentId(normalizeParentId(request.getParentId()));
        menu.setOrderIndex(request.getOrderIndex());
        menu.setRequiredPermission(request.getRequiredPermission() != null && !request.getRequiredPermission().isBlank()
                ? request.getRequiredPermission().trim()
                : null);
        menu.setActive(request.getActive());

    }

    private String normalizeParentId(String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return null;
        }
        return parentId.trim();
    }

    private List<MenuTreeResponse> buildMenuTree(List<Menu> menus) {

        Map<String, MenuTreeResponse> menuMap = new HashMap<>();
        Map<String, Menu> menuById = new HashMap<>();

        List<MenuTreeResponse> rootMenus = new ArrayList<>();

        // Convert every Menu into MenuTreeResponse
        for (Menu menu : menus) {
            menuMap.put(
                    menu.getId(),
                    menuMapper.toMenuTreeResponse(menu));
            menuById.put(menu.getId(), menu);
        }

        // Build the hierarchy
        for (Menu menu : menus) {

            MenuTreeResponse currentMenu = menuMap.get(menu.getId());

            if (menu.getParentId() == null || menu.getParentId().isBlank()) {
                rootMenus.add(currentMenu);
            } else {

                MenuTreeResponse parentMenu = menuMap.get(menu.getParentId());

                if (parentMenu != null && !createsCycle(menu.getId(), menu.getParentId(), menuById)) {
                    parentMenu.getChildren().add(currentMenu);
                } else {
                    // Parent is missing/filtered or cyclic; promote to root level to avoid orphaning
                    rootMenus.add(currentMenu);
                }
            }
        }
        return rootMenus;
    }

    private boolean createsCycle(String childId, String parentId, Map<String, Menu> menuById) {
        String currentId = parentId;
        Set<String> visited = new HashSet<>();
        while (currentId != null) {
            if (currentId.equals(childId)) {
                return true;
            }
            if (!visited.add(currentId)) {
                return true;
            }
            Menu parent = menuById.get(currentId);
            currentId = parent != null ? parent.getParentId() : null;
        }
        return false;
    }

    private List<MenuResponse> mapToMenuResponseList(List<Menu> menus) {

        return menus.stream()
                .map(menuMapper::toMenuResponse)
                .toList();
    }
}
