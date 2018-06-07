package uk.co.gmescouts.stmarys.beddingplants.exports;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.co.gmescouts.stmarys.beddingplants.exports.service.ExportService;

@RestController
@RequestMapping(value = "/export")
public class ExportPdf {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExportPdf.class);

	/*
	 * Orders
	 */
	private final static String EXPORT_CUSTOMER_ORDERS = "/orders";
	private final static String EXPORT_CUSTOMER_ORDERS_PDF = EXPORT_CUSTOMER_ORDERS + "/pdf";

	@Resource
	private ExportService exportService;

	@GetMapping(produces = MediaType.APPLICATION_PDF_VALUE, value = EXPORT_CUSTOMER_ORDERS_PDF)
	public ResponseEntity<ByteArrayResource> exportSaleCustomerOrdersAsPdf(@RequestParam final Integer saleYear) {
		LOGGER.info("Exporting Order details for Sale [{}]", saleYear);

		// do the export
		// TODO: call the HTML export endpoint and use pdfHTML
		final byte[] pdf = exportService.exportSaleCustomersToPdf(saleYear);

		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
		headers.add(HttpHeaders.PRAGMA, "no-cache");
		headers.add(HttpHeaders.EXPIRES, "0");
		headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"sale_orders_%s.pdf\"", saleYear));

		if (pdf == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok().headers(headers).contentLength(pdf.length).contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new ByteArrayResource(pdf));
	}
}