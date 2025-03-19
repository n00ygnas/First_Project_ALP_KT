package com.kt.fire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FireApplication {

	public static void main(String[] args) {
		SpringApplication.run(FireApplication.class, args);
	}

}
