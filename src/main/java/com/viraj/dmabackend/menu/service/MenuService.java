package com.viraj.dmabackend.menu.service;

import com.viraj.dmabackend.menu.dto.CreateMenuRequest;
import com.viraj.dmabackend.menu.dto.MenuResponse;
import com.viraj.dmabackend.menu.dto.MenuTreeResponse;
import com.viraj.dmabackend.menu.dto.UpdateMenuRequest;

import java.util.List;

public interface MenuService {

    MenuResponse createMenu(CreateMenuRequest request);

    MenuResponse updateMenu(String menuId, UpdateMenuRequest request);

    MenuResponse getMenuById(String menuId);

    List<MenuResponse> getAllMenus();

    List<MenuTreeResponse> getMenuTree();

    void deactivateMenu(String menuId);
}
