package com.viraj.dmabackend.menu.validator;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.menu.entity.Menu;
import com.viraj.dmabackend.menu.exception.CircularMenuException;
import com.viraj.dmabackend.menu.exception.DuplicateMenuPathException;
import com.viraj.dmabackend.menu.exception.DuplicateMenuTitleException;
import com.viraj.dmabackend.menu.exception.InvalidMenuPathException;
import com.viraj.dmabackend.menu.exception.ParentMenuNotFoundException;
import com.viraj.dmabackend.menu.exception.SelfParentMenuException;
import com.viraj.dmabackend.menu.repository.MenuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuValidatorTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuValidator menuValidator;

    @Test
    void validateParentExists_whenParentNotFound_throwsException() {
        when(menuRepository.findById("parent-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuValidator.validateParentExists("parent-1"))
                .isInstanceOf(ParentMenuNotFoundException.class);
    }

    @Test
    void validateParentExists_whenParentIsInactive_throwsBadRequestException() {
        Menu parent = Menu.builder().id("parent-1").title("Parent").active(false).build();
        when(menuRepository.findById("parent-1")).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> menuValidator.validateParentExists("parent-1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot assign inactive menu as parent");
    }

    @Test
    void validateParentExists_whenParentIsActive_succeeds() {
        Menu parent = Menu.builder().id("parent-1").title("Parent").active(true).build();
        when(menuRepository.findById("parent-1")).thenReturn(Optional.of(parent));

        assertThatCode(() -> menuValidator.validateParentExists("parent-1"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateParentExists_whenParentHasSurroundingWhitespace_trimsAndFindsParent() {
        Menu parent = Menu.builder().id("parent-1").title("Parent").active(true).build();
        when(menuRepository.findById("parent-1")).thenReturn(Optional.of(parent));

        assertThatCode(() -> menuValidator.validateParentExists("  parent-1  "))
                .doesNotThrowAnyException();
    }

    @Test
    void validateMenuDeactivation_whenActiveChildrenExist_throwsBadRequestException() {
        when(menuRepository.existsByParentIdAndActiveTrue("menu-1")).thenReturn(true);

        assertThatThrownBy(() -> menuValidator.validateMenuDeactivation("menu-1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot deactivate menu with active submenus");
    }

    @Test
    void validateMenuDeactivation_whenNoActiveChildrenExist_succeeds() {
        when(menuRepository.existsByParentIdAndActiveTrue("menu-1")).thenReturn(false);

        assertThatCode(() -> menuValidator.validateMenuDeactivation("menu-1"))
                .doesNotThrowAnyException();
    }

    @Test
    void validatePath_withoutLeadingSlash_throwsException() {
        assertThatThrownBy(() -> menuValidator.validatePath("leads"))
                .isInstanceOf(InvalidMenuPathException.class);
    }

    @Test
    void validatePath_withTrailingSlash_throwsException() {
        assertThatThrownBy(() -> menuValidator.validatePath("/leads/"))
                .isInstanceOf(InvalidMenuPathException.class);
    }

    @Test
    void validatePath_withDuplicateSlashes_throwsException() {
        assertThatThrownBy(() -> menuValidator.validatePath("//leads"))
                .isInstanceOf(InvalidMenuPathException.class);
    }

    @Test
    void validatePath_rootSlash_succeeds() {
        assertThatCode(() -> menuValidator.validatePath("/"))
                .doesNotThrowAnyException();
    }

    @Test
    void validatePath_whenNull_throwsInvalidMenuPathException() {
        assertThatThrownBy(() -> menuValidator.validatePath(null))
                .isInstanceOf(InvalidMenuPathException.class);
    }

    @Test
    void validatePath_whenValidLeadingSlash_succeeds() {
        assertThatCode(() -> menuValidator.validatePath("/leads"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateParentRelationship_whenSelfParent_throwsException() {
        assertThatThrownBy(() -> menuValidator.validateParentRelationship("id1", "id1"))
                .isInstanceOf(SelfParentMenuException.class)
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateParentRelationship_whenCircularReference_throwsCircularMenuException() {
        // id1 -> parent is id2; id2 -> parent is id3; id3 -> parent is id1 (circular)
        Menu menu2 = Menu.builder().id("id2").parentId("id3").build();
        Menu menu3 = Menu.builder().id("id3").parentId("id1").build();

        when(menuRepository.findById("id2")).thenReturn(Optional.of(menu2));
        when(menuRepository.findById("id3")).thenReturn(Optional.of(menu3));

        assertThatThrownBy(() -> menuValidator.validateParentRelationship("id1", "id2"))
                .isInstanceOf(CircularMenuException.class)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Circular parent reference detected. A menu cannot be an ancestor of its own parent.");
    }

    @Test
    void validateDuplicateTitle_trimsAndFindsDuplicate() {
        when(menuRepository.findByTitle("Dashboard"))
                .thenReturn(Optional.of(Menu.builder().id("m1").title("Dashboard").build()));

        assertThatThrownBy(() -> menuValidator.validateDuplicateTitle("  Dashboard  "))
                .isInstanceOf(DuplicateMenuTitleException.class);
    }

    @Test
    void validateDuplicatePath_trimsAndFindsDuplicate() {
        when(menuRepository.findByPath("/dashboard"))
                .thenReturn(Optional.of(Menu.builder().id("m1").path("/dashboard").build()));

        assertThatThrownBy(() -> menuValidator.validateDuplicatePath("  /dashboard  "))
                .isInstanceOf(DuplicateMenuPathException.class);
    }

    @Test
    void validateDuplicateTitleForUpdate_trimsAndFindsDuplicate() {
        when(menuRepository.existsByTitleAndIdNot("Settings", "m2")).thenReturn(true);

        assertThatThrownBy(() -> menuValidator.validateDuplicateTitleForUpdate("  Settings  ", "  m2  "))
                .isInstanceOf(DuplicateMenuTitleException.class);
    }

    @Test
    void validateDuplicatePathForUpdate_trimsAndFindsDuplicate() {
        when(menuRepository.existsByPathAndIdNot("/settings", "m2")).thenReturn(true);

        assertThatThrownBy(() -> menuValidator.validateDuplicatePathForUpdate("  /settings  ", "  m2  "))
                .isInstanceOf(DuplicateMenuPathException.class);
    }

    @Test
    void validateMenuDeactivation_trimsMenuId() {
        when(menuRepository.existsByParentIdAndActiveTrue("m2")).thenReturn(true);

        assertThatThrownBy(() -> menuValidator.validateMenuDeactivation("  m2  "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot deactivate menu with active submenus");
    }
}
