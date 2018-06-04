package uk.co.gmescouts.stmarys.beddingplants.exports.configuration;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;

@Configuration
public class ExportConfiguration {
	@Bean
	public HttpMessageConverters customConverters() {
		final ByteArrayHttpMessageConverter arrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
		return new HttpMessageConverters(arrayHttpMessageConverter);
	}
}
