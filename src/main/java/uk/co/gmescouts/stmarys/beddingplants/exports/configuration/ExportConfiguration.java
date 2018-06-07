package uk.co.gmescouts.stmarys.beddingplants.exports.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "beddingplants.export")
@Configuration
public class ExportConfiguration {
	@Getter
	@Setter
	@Value("${spring.application.name}")
	private String appName;

	@Bean
	public HttpMessageConverters customConverters() {
		return new HttpMessageConverters(new ByteArrayHttpMessageConverter());
	}
}
