
package com.helpdeskpro.helpdeskpro_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HelpdeskproBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelpdeskproBackendApplication.class, args);
	}
}
