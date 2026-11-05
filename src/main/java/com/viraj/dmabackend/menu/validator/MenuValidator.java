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

        if (menuRepository.findByTitle(title).isPresent()) {
            throw new DuplicateMenuTitleException(title);
        }
    }

    public void validateDuplicatePath(String path) {

        if (menuRepository.findByPath(path).isPresent()) {
            throw new DuplicateMenuPathException(path);
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

        if (menuId != null && menuRepository.existsByParentIdAndActiveTrue(menuId)) {
            throw new BadRequestException("Cannot deactivate menu with active submenus. Deactivate child menus first.");
        }
    }

    public void validateParentRelationship(String menuId, String parentId) {

        if (menuId == null || parentId == null || parentId.isBlank()) {
            return;
        }

        String trimmedParentId = parentId.trim();
        if (menuId.equals(trimmedParentId)) {
            throw new SelfParentMenuException();
        }

        String currentParent = trimmedParentId;
        Set<String> visited = new HashSet<>();

        while (currentParent != null) {

            if (currentParent.equals(menuId)) {
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

        if (path == null || !path.trim().startsWith("/")) {
            throw new InvalidMenuPathException(path);
        }
    }

    public void validateDuplicateTitleForUpdate(String title, String menuId) {

        if (menuRepository.existsByTitleAndIdNot(title, menuId)) {
            throw new DuplicateMenuTitleException(title);
        }
    }

    public void validateDuplicatePathForUpdate(String path, String menuId) {

        if (menuRepository.existsByPathAndIdNot(path, menuId)) {
            throw new DuplicateMenuPathException(path);
        }
    }
}
