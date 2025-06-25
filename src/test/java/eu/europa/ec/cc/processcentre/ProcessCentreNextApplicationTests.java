package eu.europa.ec.cc.processcentre;

import eu.europa.ec.cc.processcentre.process.query.CommonColumnsQueriesImpl;
import eu.europa.ec.cc.processcentre.process.query.SortableFieldsQueriesImpl;
import eu.europa.ec.cc.processcentre.security.SecurityRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

	@MockitoBean
	protected SecurityRepository securityRepository;

	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry registry) {
		SingletonPostgresContainer container = SingletonPostgresContainer.getInstance();
		System.out.println("user:"+container.getUsername());
		System.out.println("pass:"+container.getPassword());
		System.out.println("url:"+container.getJdbcUrl());
		registry.add("spring.datasource.url", container::getJdbcUrl);
		registry.add("spring.datasource.username", container::getUsername);
		registry.add("spring.datasource.password", container::getPassword);
	}

	public static class SingletonPostgresContainer extends PostgreSQLContainer<SingletonPostgresContainer> {

		private static final String IMAGE_VERSION = "postgres:17";
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
