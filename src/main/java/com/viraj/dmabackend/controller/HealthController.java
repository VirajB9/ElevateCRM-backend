package com.viraj.dmabackend.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "#Health")
@RestController
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Simple endpoint to verify the backend is running.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Backend is up and running")
    })
    public com.viraj.dmabackend.common.response.ApiResponse<String> health() {

        return new com.viraj.dmabackend.common.response.ApiResponse<>(
                true,
                "Backend is running successfully",
                "OK"
        );
    }
}
