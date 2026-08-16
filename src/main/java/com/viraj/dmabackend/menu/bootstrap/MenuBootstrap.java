package com.viraj.dmabackend.menu.bootstrap;

import com.viraj.dmabackend.menu.entity.Menu;
import com.viraj.dmabackend.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(4)
public class MenuBootstrap implements CommandLineRunner {

    private final MenuRepository menuRepository;

    @Override
    public void run(String... args) {

        if (menuRepository.count() > 0) {
            return;
        }

        // ==========================================
        // 1. Root: Dashboard
        // ==========================================
        createMenu(
                "Dashboard",
                "/dashboard",
                "layout-dashboard",
                null,
                1,
                null);


        // ==========================================
        // 2. Root: Leads CRM & Submenus
        // ==========================================
        Menu leadsRoot = createMenu(
                "Leads CRM",
                "/leads",
                "users",
                null,
                2,
                "lead:read");
        createMenu(
                "All Leads",
                "/leads/all",
                "list",
                leadsRoot.getId(),
                1,
                "lead:read");
        createMenu(
                "Create Lead",
                "/leads/new",
                "user-plus",
                leadsRoot.getId(),
                2,
                "lead:create");


        // ==========================================
        // 3. Root: Client Management & Submenus
        // ==========================================
        Menu clientsRoot = createMenu(
                "Client Management",
                "/clients",
                "briefcase",
                null,
                3,
                "client:read");
        createMenu(
                "All Clients",
                "/clients/all",
                "building",
                clientsRoot.getId(),
                1,
                "client:read");
        createMenu(
                "Add Client",
                "/clients/new",
                "plus-circle",
                clientsRoot.getId(),
                2,
                "client:create");


        // ==========================================
        // 4. Root: Project Management & Submenus
        // ==========================================
        Menu projectsRoot = createMenu(
                "Project Management",
                "/projects",
                "folder-kanban",
                null,
                4,
                "project:read");
        createMenu(
                "All Projects",
                "/projects/all",
                "layers",
                projectsRoot.getId(),
                1,
                "project:read");
        createMenu(
                "New Project",
                "/projects/new",
                "folder-plus",
                projectsRoot.getId(),
                2,
                "project:create");


        // ==========================================
        // 5. Root: Invoice & Billing & Submenus
        // ==========================================
        Menu invoicesRoot = createMenu(
                "Invoice & Billing",
                "/invoices",
                "receipt",
                null,
                5,
                "invoice:read");
        createMenu(
                "All Invoices",
                "/invoices/all",
                "file-text",
                invoicesRoot.getId(),
                1,
                "invoice:read");
        createMenu(
                "Create Invoice",
                "/invoices/new",
                "file-plus",
                invoicesRoot.getId(),
                2,
                "invoice:create");


        // ==========================================
        // 6. Root: System Administration & Submenus
        // ==========================================
        Menu settingsRoot = createMenu(
                "System Administration",
                "/settings",
                "settings",
                null,
                6,
                "role:read");
        createMenu(
                "User Accounts",
                "/settings/users",
                "user-cog",
                settingsRoot.getId(),
                1,
                "user:read");
        createMenu(
                "Roles & Permissions",
                "/settings/roles",
                "shield",
                settingsRoot.getId(),
                2,
                "role:read");
    }

    private Menu createMenu(String title, String path, String icon, String parentId, Integer orderIndex, String requiredPermission) {
        Menu menu = Menu.builder()
                .title(title)
                .path(path)
                .icon(icon)
                .parentId(parentId)
                .orderIndex(orderIndex)
                .requiredPermission(requiredPermission)
                .active(true)
                .build();

        return menuRepository.save(menu);
    }
}
