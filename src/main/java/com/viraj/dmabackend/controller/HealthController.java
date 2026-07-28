package com.viraj.dmabackend.controller;

import com.viraj.dmabackend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "#Health")
@RestController
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<String> health() {

        return new ApiResponse<>(
                true,
                "Backend is running successfully",
                "OK"
        );
    }
}
