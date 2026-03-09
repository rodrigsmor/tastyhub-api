package com.rodrigo.tastyhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class TastyhubApplication {
	public static void main(String[] args) {
		SpringApplication.run(TastyhubApplication.class, args);
	}
}
