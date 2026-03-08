package com.aerobook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AeroBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(AeroBookApplication.class, args);
	}

}
