package uk.co.gmescouts.stmarys.beddingplants;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import uk.co.gmescouts.stmarys.beddingplants.configuration.BeddingPlantsConfiguration;
import uk.co.gmescouts.stmarys.beddingplants.exports.configuration.ExportConfiguration;
import uk.co.gmescouts.stmarys.beddingplants.geolocation.configuration.GeolocationConfiguration;
import uk.co.gmescouts.stmarys.beddingplants.imports.configuration.ImportConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "classpath:application.properties")
@ContextConfiguration(classes = { BeddingPlantsConfiguration.class, ImportConfiguration.class, GeolocationConfiguration.class,
		ExportConfiguration.class })
public class BeddingPlantsApplicationTests {
	@Test
	public void contextLoads() {
		// intentionally blank
	}
}
