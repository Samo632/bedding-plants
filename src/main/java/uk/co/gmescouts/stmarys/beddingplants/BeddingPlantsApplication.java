package uk.co.gmescouts.stmarys.beddingplants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SuppressWarnings("WeakerAccess")
@SpringBootApplication(scanBasePackageClasses = { uk.co.gmescouts.stmarys.beddingplants.BeddingPlantsApplication.class })
public class BeddingPlantsApplication {
	// FIXME: enable security (see pom.xml)
	public static void main(final String[] args) {
		SpringApplication.run(BeddingPlantsApplication.class, args);
	}
}
