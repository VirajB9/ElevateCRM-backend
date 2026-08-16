package com.viraj.dmabackend.auth.enums;

public enum PermissionType {

    // ===========================
    // USER MANAGEMENT
    // ===========================
    USER_CREATE,
    USER_READ,
    USER_UPDATE,
    USER_DELETE,

    // ===========================
    // CLIENT MANAGEMENT
    // ===========================
    CLIENT_CREATE,
    CLIENT_READ,
    CLIENT_UPDATE,
    CLIENT_DELETE,

    // ===========================
    // LEAD MANAGEMENT
    // ===========================
    LEAD_CREATE,
    LEAD_READ,
    LEAD_UPDATE,
    LEAD_DELETE,
    LEAD_CONVERT,

    // ===========================
    // PROJECT MANAGEMENT
    // ===========================
    PROJECT_CREATE,
    PROJECT_READ,
    PROJECT_UPDATE,
    PROJECT_DELETE,

    // ===========================
    // INVOICE MANAGEMENT
    // ===========================
    INVOICE_CREATE,
    INVOICE_READ,
    INVOICE_UPDATE,
    INVOICE_DELETE,

    // ===========================
    // ROLE MANAGEMENT
    // ===========================
    ROLE_CREATE,
    ROLE_READ,
    ROLE_UPDATE,
    ROLE_DELETE,

    // ===========================
    // MENU MANAGEMENT
    // ===========================
    MENU_CREATE,
    MENU_READ,
    MENU_UPDATE,
    MENU_DELETE
}