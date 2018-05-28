package uk.co.gmescouts.stmarys.beddingplants.imports;

import java.io.IOException;
import java.util.Calendar;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Sale;
import uk.co.gmescouts.stmarys.beddingplants.imports.service.ImportService;
import uk.co.gmescouts.stmarys.beddingplants.sales.service.SalesService;

@RestController
public class ImportController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ImportController.class);

	private final static String IMPORT_BASE_URL = "/import";

	/*
	 * Sales
	 */
	private final static String IMPORT_SALE = IMPORT_BASE_URL + "/sale";
	private final static String IMPORT_SALE_EXCEL = IMPORT_SALE + "/excel";

	/*
	 * Orders
	 */
	private final static String IMPORT_ORDERS = IMPORT_BASE_URL + "/orders";
	private final static String IMPORT_ORDERS_EXCEL = IMPORT_ORDERS + "/excel";

	/*
	 * Plants
	 */
	private final static String IMPORT_PLANTS = IMPORT_BASE_URL + "/plants";
	private final static String IMPORT_PLANTS_EXCEL = IMPORT_PLANTS + "/excel";

	private final static Integer CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);

	private final static String MEDIA_TYPE_XLS = "application/vnd.ms-excel";
	private final static String MEDIA_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	@Resource
	private ImportService importService;

	@Resource
	private SalesService salesService;

	@PostMapping(consumes = { MEDIA_TYPE_XLS, MEDIA_TYPE_XLSX,
			MediaType.MULTIPART_FORM_DATA_VALUE }, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = IMPORT_SALE_EXCEL)
	public Sale importSaleFromExcel(@RequestParam final MultipartFile file, @RequestParam(defaultValue = "20.0") final float vat,
			@RequestParam(required = false) final Integer year, @RequestParam(required = false) final String orderImportsSheetName,
			@RequestParam(required = false) final String plantImportsSheetName)
			throws EncryptedDocumentException, InvalidFormatException, IOException {

		Integer saleYear;
		if (year == null) {
			// default to current year if orderYear not specified in request
			saleYear = CURRENT_YEAR;
			LOGGER.info("Year not specified, defaulting to [{}]", CURRENT_YEAR);
		} else {
			// validate year is sensible (e.g. not in future and not too far in the past)
			if (year > CURRENT_YEAR) {
				throw new IllegalArgumentException("Cannot import information for a future year");
			}

			if (year < CURRENT_YEAR - 5) {
				throw new IllegalArgumentException("Cannot import information more than 5 years old");
			}

			saleYear = year;
		}

		try {
			// do the import
			Sale sale = importService.importSaleFromExcelFile(file, saleYear, vat, orderImportsSheetName, plantImportsSheetName);

			// save the created Sale
			sale = salesService.saveSale(sale);

			// TODO: just return a summary of the Sale rather than the entire detail
			return sale;
		} catch (final Exception e) {
			LOGGER.error(String.format("Error during import: %s", e.getMessage()), e);
			throw e;
		}
	}

	@PostMapping(consumes = { MEDIA_TYPE_XLS, MEDIA_TYPE_XLSX,
			MediaType.MULTIPART_FORM_DATA_VALUE }, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = IMPORT_ORDERS_EXCEL)
	public Set<Order> importOrdersFromExcel(@RequestParam final MultipartFile file, @RequestParam(required = true) final Integer saleYear,
			@RequestParam(required = false) final String orderImportsSheetName)
			throws EncryptedDocumentException, InvalidFormatException, IOException {
		try {
			// get the Sale using the Year
			Sale sale = salesService.findSaleByYear(saleYear);

			// do the import
			sale = importService.importOrdersToSaleFromExcelFile(file, orderImportsSheetName, sale);

			// save the updated Sale
			sale = salesService.saveSale(sale);

			// TODO: just return a summary of the Orders rather than the entire detail
			return sale.getOrders();
		} catch (final Exception e) {
			LOGGER.error(String.format("Error during import: %s", e.getMessage()), e);
			throw e;
		}
	}

	@PostMapping(consumes = { MEDIA_TYPE_XLS, MEDIA_TYPE_XLSX,
			MediaType.MULTIPART_FORM_DATA_VALUE }, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = IMPORT_PLANTS_EXCEL)
	public Set<Plant> importPlantsFromExcel(@RequestParam final MultipartFile file, @RequestParam(required = true) final Integer saleYear,
			@RequestParam(required = false) final String plantImportsSheetName)
			throws EncryptedDocumentException, InvalidFormatException, IOException {
		try {
			// get the Sale using the Year
			Sale sale = salesService.findSaleByYear(saleYear);

			// do the import
			sale = importService.importPlantsToSaleFromExcelFile(file, plantImportsSheetName, sale);

			// save the updated Sale
			sale = salesService.saveSale(sale);

			// TODO: just return a summary of the Orders rather than the entire detail
			return sale.getPlants();
		} catch (final Exception e) {
			LOGGER.error(String.format("Error during import: %s", e.getMessage()), e);
			throw e;
		}
	}
}
