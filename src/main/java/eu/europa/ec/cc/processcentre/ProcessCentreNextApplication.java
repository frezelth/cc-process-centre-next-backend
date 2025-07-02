package eu.europa.ec.cc.processcentre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableDiscoveryClient
@EnableCaching
public class ProcessCentreNextApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessCentreNextApplication.class, args);
	}

}
