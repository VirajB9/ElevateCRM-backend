package com.viraj.dmabackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "owner")
public class OwnerProperties {

    private String email;

    private String initialPassword;

    private String phoneNumber = "9999999999";
}