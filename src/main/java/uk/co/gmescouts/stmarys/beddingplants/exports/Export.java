package uk.co.gmescouts.stmarys.beddingplants.exports;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.maps.errors.ApiException;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Address;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderType;
import uk.co.gmescouts.stmarys.beddingplants.exports.service.ExportService;
import uk.co.gmescouts.stmarys.beddingplants.geolocation.data.model.MapImageFormat;
import uk.co.gmescouts.stmarys.beddingplants.geolocation.data.model.MapType;

@RestController
@RequestMapping(value = "/export")
class Export {
	private static final Logger LOGGER = LoggerFactory.getLogger(Export.class);

	/*
	 * Orders
	 */
	private static final String EXPORT_CUSTOMER_ORDERS = "/orders/{saleYear}";
	private static final String EXPORT_CUSTOMER_ORDERS_PDF = EXPORT_CUSTOMER_ORDERS + "/pdf";

	/*
	 * Addresses
	 */
	private static final String EXPORT_CUSTOMER_ADDRESSES = "/addresses/{saleYear}";
	private static final String EXPORT_CUSTOMER_ADDRESSES_IMG = EXPORT_CUSTOMER_ADDRESSES + "/img";

	@Resource
	private ExportService exportService;

	@GetMapping(produces = MediaType.APPLICATION_PDF_VALUE, value = EXPORT_CUSTOMER_ORDERS_PDF)
	public ResponseEntity<ByteArrayResource> exportSaleCustomerOrdersAsPdf(@PathVariable final Integer saleYear,
			@RequestParam(required = false) final OrderType orderType) throws IOException {
		LOGGER.info("Exporting (PDF) Order details for Sale [{}] with Order Type [{}]", saleYear, orderType);

		// get the PDF content
		final byte[] pdf = exportService.exportSaleCustomersToPdf(saleYear, orderType);

		if (pdf == null) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok().headers(getNoCacheHeaders(String.format("attachment; filename=\"sale_orders_%s.pdf\"", saleYear)))
				.contentLength(pdf.length).contentType(MediaType.APPLICATION_OCTET_STREAM).body(new ByteArrayResource(pdf));
	}

	@GetMapping(EXPORT_CUSTOMER_ADDRESSES)
	public Set<Address> exportSaleAddressesAsJson(@PathVariable final Integer saleYear, @RequestParam(required = false) final OrderType orderType,
			@RequestParam(required = false, defaultValue = "false") final boolean geolocatedOnly) {
		LOGGER.info("Exporting (JSON); Addresses for Sale [{}] with Order Type [{}] and Geolocated [{}]", saleYear, orderType, geolocatedOnly);

		// get the Addresses
		return exportService.getSaleAddresses(saleYear, orderType, geolocatedOnly);
	}

	@GetMapping(EXPORT_CUSTOMER_ADDRESSES_IMG)
	public ResponseEntity<ByteArrayResource> exportSaleAddressesAsImage(final Model model, @PathVariable final Integer saleYear,
			@RequestParam(required = false) final OrderType orderType,
			@RequestParam(defaultValue = "PNG") final MapImageFormat mapImageFormat,
			@RequestParam(defaultValue = "ROADMAP") final MapType mapType) throws ApiException, InterruptedException, IOException {
		LOGGER.info("Exporting (IMG); Addresses for Sale [{}] with Order Type [{}]", saleYear, orderType);

		// get the image
		final byte[] mapImg = exportService.exportGeolocatedSaleAddressesToImage(saleYear, orderType, mapImageFormat, mapType);

		if (mapImg == null) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok()
				.headers(getNoCacheHeaders(String.format("attachment; filename=\"map_sale_orders_%s%s.%s\"", saleYear,
						(orderType == null ? "" : orderType), mapImageFormat.getFilenameExtension())))
				.contentLength(mapImg.length).contentType(MediaType.APPLICATION_OCTET_STREAM).body(new ByteArrayResource(mapImg));
	}

	private static HttpHeaders getNoCacheHeaders(final String contentDisposition) {
		final HttpHeaders headers = new HttpHeaders();

		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
		headers.add(HttpHeaders.PRAGMA, "no-cache");
		headers.add(HttpHeaders.EXPIRES, "0");

		if (contentDisposition != null) {
			headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
		}

		return headers;
	}
}
