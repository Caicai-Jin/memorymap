package com.memorymap.memorymap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MemorymapApplication {

	public static void main(String[] args) {
		SpringApplication.run(MemorymapApplication.class, args);
	}

}
