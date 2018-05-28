package uk.co.gmescouts.stmarys.beddingplants.sales;

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Sale;
import uk.co.gmescouts.stmarys.beddingplants.sales.service.SalesService;

@RestController
public class SalesController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SalesController.class);

	public final static String SALE_BASE_URL = "/sales";

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

	@Resource
	private SalesService salesService;

	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = SALE_SUMMARY)
	public Set<Integer> geSaleSummary() {
		LOGGER.info("Retrieving Sale summaries");

		// TODO: return more summary details (order count, plant count, total cost, total income, profit)
		final Set<Integer> saleYears = salesService.findAllSales().stream().map(Sale::getYear).sorted().collect(Collectors.toSet());

		LOGGER.info("Sale summaries: [{}]", saleYears);

		return saleYears;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = SALE_DETAIL)
	public Sale getSaleDetail(@RequestParam(required = true) final Integer year) {
		LOGGER.info("Finding details for Sale year [{}]", year);

		final Sale sale = salesService.findSaleByYear(year);

		LOGGER.debug("Sale: [{}]", sale);

		return sale;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = SALE_DETAIL_ORDER)
	public Order getOrderDetail(@RequestParam(required = true) final Integer year, @RequestParam(required = true) final Integer orderNumber) {
		LOGGER.info("Finding details for Order [{}] from Sale year [{}]", orderNumber, year);

		final Order order = salesService.findOrderByNumAndSaleYear(orderNumber, year);

		LOGGER.debug("Order: [{}]", order);

		return order;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = SALE_DETAIL_PLANT)
	public Plant getPlantDetail(@RequestParam(required = true) final Integer year, @RequestParam(required = true) final Integer plantNumber) {
		LOGGER.info("Finding details for Plant [{}] from Sale year [{}]", plantNumber, year);

		final Plant plant = salesService.findPlantByNumAndSaleYear(plantNumber, year);

		LOGGER.debug("Plant: [{}]", plant);

		return plant;
	}
}
