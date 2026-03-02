package com.maninder.fileBrain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FileBrainApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileBrainApplication.class, args);
	}

}
