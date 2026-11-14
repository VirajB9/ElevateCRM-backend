package com.viraj.dmabackend.auth.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthControllerOpenApiTest {

    @Test
    void rateLimitedEndpoints_DeclareHttp429ResponseCode() {
        List<String> rateLimitedMethods = List.of("login", "refresh", "logout", "changePassword");

        for (Method method : AuthController.class.getDeclaredMethods()) {
            if (rateLimitedMethods.contains(method.getName())) {
                ApiResponses apiResponses = method.getAnnotation(ApiResponses.class);
                assertTrue(apiResponses != null, "Method " + method.getName() + " must have @ApiResponses annotation");

                boolean has429 = Arrays.stream(apiResponses.value())
                        .anyMatch(resp -> "429".equals(resp.responseCode()));

                assertTrue(has429, "Method " + method.getName() + " must declare @ApiResponse(responseCode = '429')");
            }
        }
    }

    @Test
    void logoutEndpoint_DoesNotDeclareHttp401SinceItIsIdempotent() throws NoSuchMethodException {
        Method logoutMethod = AuthController.class.getDeclaredMethod("logout", com.viraj.dmabackend.auth.dto.LogoutRequest.class);
        ApiResponses apiResponses = logoutMethod.getAnnotation(ApiResponses.class);
        boolean has401 = Arrays.stream(apiResponses.value()).anyMatch(resp -> "401".equals(resp.responseCode()));
        org.junit.jupiter.api.Assertions.assertFalse(has401, "Logout endpoint should not declare 401 as it is an idempotent operation");
    }
}
