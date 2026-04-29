package com.picknquicks;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.DockerClientFactory;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PicknquicksApplicationTests {

	@BeforeAll
	static void requireDockerForTestcontainers() {
		Assumptions.assumeTrue(
				DockerClientFactory.instance().isDockerAvailable(),
				"Skipping context test: Docker is not available for Testcontainers"
		);
	}

	@Test
	void contextLoads() {
	}

}
