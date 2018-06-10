package uk.co.gmescouts.stmarys.beddingplants.exports.configuration;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@ConfigurationProperties(prefix = "beddingplants.export")
@Configuration
public class ExportConfiguration {
	@Bean
	public HttpMessageConverters customConverters() {
		return new HttpMessageConverters(new ByteArrayHttpMessageConverter());
	}

	@Bean
	public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder.build();
	}
}
