package com.viraj.dmabackend.menu.validator;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.menu.entity.Menu;
import com.viraj.dmabackend.menu.exception.DuplicateMenuPathException;
import com.viraj.dmabackend.menu.exception.DuplicateMenuTitleException;
import com.viraj.dmabackend.menu.exception.InvalidMenuPathException;
import com.viraj.dmabackend.menu.exception.ParentMenuNotFoundException;
import com.viraj.dmabackend.menu.exception.SelfParentMenuException;
import com.viraj.dmabackend.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import com.viraj.dmabackend.menu.exception.CircularMenuException;

import java.util.Set;


@Component
@RequiredArgsConstructor
public class MenuValidator {

    private final MenuRepository menuRepository;

    public void validateDuplicateTitle(String title) {
        if (title == null) {
            return;
        }
        String trimmedTitle = title.trim();
        if (menuRepository.findByTitle(trimmedTitle).isPresent()) {
            throw new DuplicateMenuTitleException(trimmedTitle);
        }
    }

    public void validateDuplicatePath(String path) {
        if (path == null) {
            return;
        }
        String trimmedPath = path.trim();
        if (menuRepository.findByPath(trimmedPath).isPresent()) {
            throw new DuplicateMenuPathException(trimmedPath);
        }
    }

    public void validateParentExists(String parentId) {

        if (parentId == null || parentId.isBlank()) {
            return;
        }
        String trimmedParentId = parentId.trim();
        Menu parent = menuRepository.findById(trimmedParentId)
                .orElseThrow(() -> new ParentMenuNotFoundException(trimmedParentId));
        if (Boolean.FALSE.equals(parent.getActive())) {
            throw new BadRequestException("Cannot assign inactive menu as parent");
        }
    }

    public void validateMenuDeactivation(String menuId) {
        String trimmedMenuId = menuId != null ? menuId.trim() : null;
        if (trimmedMenuId != null && menuRepository.existsByParentIdAndActiveTrue(trimmedMenuId)) {
            throw new BadRequestException("Cannot deactivate menu with active submenus. Deactivate child menus first.");
        }
    }

    public void validateParentRelationship(String menuId, String parentId) {

        if (menuId == null || parentId == null || parentId.isBlank()) {
            return;
        }

        String trimmedMenuId = menuId.trim();
        String trimmedParentId = parentId.trim();
        if (trimmedMenuId.equals(trimmedParentId)) {
            throw new SelfParentMenuException();
        }

        String currentParent = trimmedParentId;
        Set<String> visited = new HashSet<>();

        while (currentParent != null) {

            if (currentParent.equals(trimmedMenuId)) {
                throw new CircularMenuException();
            }

            if (!visited.add(currentParent)) {
                break;
            }

            Menu parent = menuRepository.findById(currentParent).orElse(null);

            currentParent = parent != null ? parent.getParentId() : null;
        }
    }

    public void validatePath(String path) {
        if (path == null) {
            throw new InvalidMenuPathException(path);
        }
        String trimmed = path.trim();
        if (!trimmed.startsWith("/") || (trimmed.length() > 1 && trimmed.endsWith("/")) || trimmed.contains("//")) {
            throw new InvalidMenuPathException(path);
        }
    }

    public void validateDuplicateTitleForUpdate(String title, String menuId) {
        if (title == null) {
            return;
        }
        String trimmedTitle = title.trim();
        String trimmedMenuId = menuId != null ? menuId.trim() : null;
        if (menuRepository.existsByTitleAndIdNot(trimmedTitle, trimmedMenuId)) {
            throw new DuplicateMenuTitleException(trimmedTitle);
        }
    }

    public void validateDuplicatePathForUpdate(String path, String menuId) {
        if (path == null) {
            return;
        }
        String trimmedPath = path.trim();
        String trimmedMenuId = menuId != null ? menuId.trim() : null;
        if (menuRepository.existsByPathAndIdNot(trimmedPath, trimmedMenuId)) {
            throw new DuplicateMenuPathException(trimmedPath);
        }
    }
}
