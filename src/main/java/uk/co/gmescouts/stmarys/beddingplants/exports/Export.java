package uk.co.gmescouts.stmarys.beddingplants.exports;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.annotation.Resource;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.co.gmescouts.stmarys.beddingplants.exports.service.ExportService;

@RestController
public class Export {
	private static final Logger LOGGER = LoggerFactory.getLogger(Export.class);

	private final static String EXPORT_BASE_URL = "/export";

	/*
	 * Customers
	 */
	private final static String EXPORT_CUSTOMERS = EXPORT_BASE_URL + "/customers";
	private final static String EXPORT_CUSTOMERS_PDF = EXPORT_CUSTOMERS + "/pdf";

	@Resource
	private ExportService exportService;

	@GetMapping(produces = MediaType.APPLICATION_PDF_VALUE, value = EXPORT_CUSTOMERS_PDF)
	public ResponseEntity<InputStreamResource> exportSaleCustomersToPdf(@RequestParam final Integer saleYear)
			throws EncryptedDocumentException, InvalidFormatException, IOException {
		LOGGER.info("Exporting Order details for Sale [{}]", saleYear);

		// do the export
		final byte[] pdf = exportService.exportSaleCustomersToPdf(saleYear);

		final HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");

		return ResponseEntity.ok().headers(headers).contentLength(pdf == null ? 0 : pdf.length)
				.contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
				.body(new InputStreamResource(new ByteArrayInputStream(pdf)));
	}
}
