package uk.co.gmescouts.stmarys.beddingplants.sales;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Sale;
import uk.co.gmescouts.stmarys.beddingplants.sales.data.model.SaleSummary;
import uk.co.gmescouts.stmarys.beddingplants.sales.service.SalesService;

@RestController
public class Sales {
	private static final Logger LOGGER = LoggerFactory.getLogger(Sales.class);

	private final static String SALE_BASE_URL = "/sales";

	/*
	 * Summaries
	 */
	private final static String SALE_SUMMARY = SALE_BASE_URL + "/summary";

	/*
	 * Details
	 */
	private final static String SALE_DETAIL = SALE_BASE_URL + "/detail";
	private final static String SALE_DETAIL_ORDER = SALE_SUMMARY + "/order";
	private final static String SALE_DETAIL_PLANT = SALE_SUMMARY + "/plant";

	/*
	 * Deletes
	 */
	private final static String DELETE_SALE = SALE_BASE_URL;
	private final static String DELETE_ORDER = SALE_BASE_URL + "/order";
	private final static String DELETE_PLANT = SALE_BASE_URL + "/plant";

	@Resource
	private SalesService salesService;

	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = SALE_SUMMARY)
	public Set<SaleSummary> geSaleSummary() {
		LOGGER.info("Retrieving Sale summaries");

		final Set<SaleSummary> saleSummaries = salesService.findAllSales().stream().map(salesService::summariseSale)
				.sorted(Comparator.comparingInt(SaleSummary::getYear)).collect(Collectors.toSet());

		LOGGER.info("Number of Sales [{}]", saleSummaries);

		return saleSummaries;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = SALE_DETAIL)
	public Sale getSaleDetail(@RequestParam final Integer year) {
		LOGGER.info("Finding details for Sale year [{}]", year);

		final Sale sale = salesService.findSaleByYear(year);

		LOGGER.debug("Sale: [{}]", sale);

		return sale;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = SALE_DETAIL_ORDER)
	public Order getOrderDetail(@RequestParam final Integer year, @RequestParam final Integer orderNumber) {
		LOGGER.info("Finding details for Order [{}] from Sale year [{}]", orderNumber, year);

		final Order order = salesService.findOrderByNumAndSaleYear(orderNumber, year);

		LOGGER.debug("Order: [{}]", order);

		return order;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = SALE_DETAIL_PLANT)
	public Plant getPlantDetail(@RequestParam final Integer year, @RequestParam final Integer plantNumber) {
		LOGGER.info("Finding details for Plant [{}] from Sale year [{}]", plantNumber, year);

		final Plant plant = salesService.findPlantByNumAndSaleYear(plantNumber, year);

		LOGGER.debug("Plant: [{}]", plant);

		return plant;
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = DELETE_SALE)
	public Boolean deleteSale(@RequestParam final Integer year) {
		LOGGER.info("Deleting Sale [{}]", year);

		final boolean deleted = salesService.deleteSale(year);

		LOGGER.debug("Sale [{}] deleted [{}]", year, deleted);

		return deleted;
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = DELETE_ORDER)
	public Boolean deleteOrder(@RequestParam final Integer orderNumber, @RequestParam final Integer year) {
		LOGGER.info("Deleting Order [{}] from Sale [{}]", orderNumber, year);

		final boolean deleted = salesService.deleteOrder(orderNumber, year);

		LOGGER.debug("Order [{}] from Sale [{}] deleted [{}]", orderNumber, year, deleted);

		return deleted;
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = DELETE_PLANT)
	public Boolean deletePlant(@RequestParam final Integer plantNumber, @RequestParam final Integer year) {
		LOGGER.info("Deleting Plant [{}] from Sale [{}]", plantNumber, year);

		final boolean deleted = salesService.deletePlant(plantNumber, year);

		LOGGER.debug("Plant [{}] from Sale [{}] deleted [{}]", plantNumber, year, deleted);

		return deleted;
	}
}
