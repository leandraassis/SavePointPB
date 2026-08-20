package com.assis.gamelog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GamelogApplication {

	public static void main(String[] args) {
		SpringApplication.run(GamelogApplication.class, args);
	}

}
