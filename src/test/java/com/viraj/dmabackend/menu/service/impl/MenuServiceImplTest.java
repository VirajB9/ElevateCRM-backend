package com.viraj.dmabackend.menu.service.impl;

import com.viraj.dmabackend.menu.dto.MenuTreeResponse;
import com.viraj.dmabackend.menu.entity.Menu;
import com.viraj.dmabackend.menu.mapper.MenuMapper;
import com.viraj.dmabackend.menu.repository.MenuRepository;
import com.viraj.dmabackend.menu.validator.MenuValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock
    private MenuRepository menuRepository;

    @Spy
    private MenuMapper menuMapper = new MenuMapper();

    @Mock
    private MenuValidator menuValidator;

    @InjectMocks
    private MenuServiceImpl menuService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMenuTree_whenAuthenticationIsNull_returnsUnrestrictedMenusWithoutNPE() {
        Menu publicMenu = Menu.builder()
                .id("1")
                .title("Dashboard")
                .path("/dashboard")
                .orderIndex(1)
                .requiredPermission(null)
                .active(true)
                .build();

        Menu restrictedMenu = Menu.builder()
                .id("2")
                .title("Leads")
                .path("/leads")
                .orderIndex(2)
                .requiredPermission("lead:read")
                .active(true)
                .build();

        when(menuRepository.findByActiveTrueOrderByOrderIndexAsc())
                .thenReturn(List.of(publicMenu, restrictedMenu));

        List<MenuTreeResponse> result = menuService.getMenuTree();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Dashboard");
    }

    @Test
    void getMenuTree_whenAuthenticatedWithMatchingPermission_returnsAllowedMenus() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", "pass",
                        List.of(new SimpleGrantedAuthority("lead:read")))
        );

        Menu publicMenu = Menu.builder()
                .id("1")
                .title("Dashboard")
                .path("/dashboard")
                .orderIndex(1)
                .requiredPermission(null)
                .active(true)
                .build();

        Menu restrictedMenu = Menu.builder()
                .id("2")
                .title("Leads")
                .path("/leads")
                .orderIndex(2)
                .requiredPermission("lead:read")
                .active(true)
                .build();

        Menu adminOnlyMenu = Menu.builder()
                .id("3")
                .title("Users")
                .path("/users")
                .orderIndex(3)
                .requiredPermission("user:read")
                .active(true)
                .build();

        when(menuRepository.findByActiveTrueOrderByOrderIndexAsc())
                .thenReturn(List.of(publicMenu, restrictedMenu, adminOnlyMenu));

        List<MenuTreeResponse> result = menuService.getMenuTree();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MenuTreeResponse::getTitle)
                .containsExactly("Dashboard", "Leads");
    }

    @Test
    void deactivateMenu_validatesActiveChildrenBeforeDeactivating() {
        Menu menu = Menu.builder().id("menu-1").title("Parent").active(true).build();
        when(menuRepository.findById("menu-1")).thenReturn(java.util.Optional.of(menu));

        menuService.deactivateMenu("menu-1");

        org.mockito.Mockito.verify(menuValidator).validateMenuDeactivation("menu-1");
        org.mockito.Mockito.verify(menuRepository).save(menu);
        assertThat(menu.getActive()).isFalse();
    }

    @Test
    void updateMenu_whenDeactivating_validatesActiveChildren() {
        Menu menu = Menu.builder().id("menu-1").title("Parent").path("/parent").active(true).build();
        when(menuRepository.findById("menu-1")).thenReturn(java.util.Optional.of(menu));
        when(menuRepository.save(any(Menu.class))).thenReturn(menu);

        com.viraj.dmabackend.menu.dto.UpdateMenuRequest request = new com.viraj.dmabackend.menu.dto.UpdateMenuRequest();
        request.setTitle("Updated Parent");
        request.setPath("/parent");
        request.setActive(false);

        menuService.updateMenu("menu-1", request);

        org.mockito.Mockito.verify(menuValidator).validateMenuDeactivation("menu-1");
        org.mockito.Mockito.verify(menuRepository).save(menu);
        assertThat(menu.getActive()).isFalse();
    }
}
