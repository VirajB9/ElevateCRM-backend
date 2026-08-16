package com.viraj.dmabackend.menu.validator;

import com.viraj.dmabackend.menu.exception.DuplicateMenuPathException;
import com.viraj.dmabackend.menu.exception.DuplicateMenuTitleException;
import com.viraj.dmabackend.menu.exception.InvalidMenuPathException;
import com.viraj.dmabackend.menu.exception.ParentMenuNotFoundException;
import com.viraj.dmabackend.menu.exception.SelfParentMenuException;
import com.viraj.dmabackend.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
        menuRepository.findById(parentId)
                .orElseThrow(() -> new ParentMenuNotFoundException(parentId));
    }

    public void validateParentRelationship(String menuId, String parentId) {

        if (menuId == null || parentId == null) {
            return;
        }
        if (menuId.equals(parentId)) {
            throw new SelfParentMenuException();
        }
    }

    public void validatePath(String path) {

        if(!path.startsWith("/")){
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
