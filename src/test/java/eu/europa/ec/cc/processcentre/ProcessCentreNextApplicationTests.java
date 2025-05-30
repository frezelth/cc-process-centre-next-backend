package eu.europa.ec.cc.processcentre;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
public abstract class ProcessCentreNextApplicationTests {

	static {
		// initialize the postgres container
		SingletonPostgresContainer.getInstance();
	}

	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry registry) {
		SingletonPostgresContainer container = SingletonPostgresContainer.getInstance();
		registry.add("spring.datasource.url", container::getJdbcUrl);
		registry.add("spring.datasource.username", container::getUsername);
		registry.add("spring.datasource.password", container::getPassword);
	}

	public static class SingletonPostgresContainer extends PostgreSQLContainer<SingletonPostgresContainer> {

		private static final String IMAGE_VERSION = "postgres:15";
		private static SingletonPostgresContainer container;

		private SingletonPostgresContainer() {
			super(IMAGE_VERSION);
		}

		public static SingletonPostgresContainer getInstance() {
			if (container == null) {
				container = new SingletonPostgresContainer();
				container.start();
			}
			return container;
		}

		@Override
		public void stop() {
			// Do nothing, JVM shutdown hook will handle it
		}
	}

}
