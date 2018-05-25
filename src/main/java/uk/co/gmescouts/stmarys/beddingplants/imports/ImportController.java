package uk.co.gmescouts.stmarys.beddingplants.imports;

import java.io.IOException;
import java.util.Calendar;

import javax.annotation.Resource;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import uk.co.gmescouts.stmarys.beddingplants.data.SaleRepository;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Sale;
import uk.co.gmescouts.stmarys.beddingplants.imports.service.ImportService;

@RestController
public class ImportController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ImportController.class);

	public final static String IMPORT_BASE_URL = "/import";
	private final static String IMPORT_EXCEL = IMPORT_BASE_URL + "/excel";

	private final static Integer CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);

	@Resource
	private ImportService importService;

	@Autowired
	private SaleRepository saleRepository;

	@PostMapping(consumes = { "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
			MediaType.MULTIPART_FORM_DATA_VALUE }, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = IMPORT_EXCEL)
	public Sale importSale(@RequestParam final MultipartFile file, @RequestParam(defaultValue = "20.0") final float vat,
			@RequestParam(required = false) final Integer year) throws EncryptedDocumentException, InvalidFormatException, IOException {

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
			final Sale sale = importService.importSaleFromExcelFile(file, saleYear, vat);

			// store the created Sale object locally for later retrieval
			saleRepository.save(sale);

			// TODO: just return a summary of the Sale rather than the entire detail
			return sale;
		} catch (final Exception e) {
			LOGGER.error(String.format("Error during import: %s", e.getMessage()), e);
			throw e;
		}
	}
}
