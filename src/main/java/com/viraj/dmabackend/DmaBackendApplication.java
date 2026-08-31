package com.viraj.dmabackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DmaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DmaBackendApplication.class, args);
	}
}
