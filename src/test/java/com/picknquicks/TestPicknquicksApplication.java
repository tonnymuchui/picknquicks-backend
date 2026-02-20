package com.picknquicks;

import org.springframework.boot.SpringApplication;

public class TestPicknquicksApplication {

	public static void main(String[] args) {
		SpringApplication.from(PicknquicksApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
