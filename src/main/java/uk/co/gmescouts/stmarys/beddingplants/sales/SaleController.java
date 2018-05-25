package uk.co.gmescouts.stmarys.beddingplants.sales;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.co.gmescouts.stmarys.beddingplants.data.SaleRepository;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Sale;

@RestController
public class SaleController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SaleController.class);

	public final static String SALE_BASE_URL = "/sale";
	private final static String SALE_YEAR_LIST = SALE_BASE_URL + "/years";
	private final static String SALE_DETAIL = SALE_BASE_URL + "/detail";

	@Autowired
	private SaleRepository saleRepository;

	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = SALE_YEAR_LIST)
	public Set<Integer> geAllSaleYears() {
		final Set<Integer> saleYears = saleRepository.findAll().stream().map(Sale::getYear).sorted().collect(Collectors.toSet());

		LOGGER.info("Sale years: [{}]", saleYears);

		return saleYears;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = SALE_DETAIL)
	public Sale getSaleDetail(@RequestParam(required = true) final Integer year) {
		LOGGER.info("Finding details for Sale year [{}]", year);

		final Sale sale = saleRepository.findByYear(year);

		LOGGER.debug("Sale summary: [ Orders [{}], Plants [{}]]", sale.getOrders().size(), sale.getPlants().size());

		return sale;
	}
}
