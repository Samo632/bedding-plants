package uk.co.gmescouts.stmarys.beddingplants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "uk.co.gmescouts.stmarys.beddingplants")
public class BeddingPlantsApplication {

	public static void main(final String[] args) {
		SpringApplication.run(BeddingPlantsApplication.class, args);
	}
}
