package eu.europa.ec.cc.processcentre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ProcessCentreNextApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessCentreNextApplication.class, args);
	}

}
