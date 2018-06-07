package uk.co.gmescouts.stmarys.beddingplants.exports;

import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;
import uk.co.gmescouts.stmarys.beddingplants.exports.configuration.ExportConfiguration;
import uk.co.gmescouts.stmarys.beddingplants.exports.service.ExportService;

@Controller
public class ExportHtml {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExportHtml.class);

	private final static String EXPORT_BASE = "/export";

	/*
	 * Orders
	 */
	private final static String EXPORT_CUSTOMER_ORDERS = EXPORT_BASE + "/orders";
	private final static String EXPORT_CUSTOMER_ORDERS_HTML = EXPORT_CUSTOMER_ORDERS + "/html";

	@Resource
	private ExportConfiguration exportConfiguration;

	@Resource
	private ExportService exportService;

	// @RequestMapping(EXPORT_CUSTOMER_ORDERS_HTML)
	@GetMapping(EXPORT_CUSTOMER_ORDERS_HTML)
	public String exportSaleCustomerOrdersAsHtml(final Model model, @RequestParam final Integer saleYear) {
		LOGGER.info("Exporting Order details for Sale [{}]", saleYear);

		// get the Plants
		final Set<Plant> plants = exportService.getSalePlants(saleYear);

		// get the Orders
		final Set<Order> orders = exportService.getSaleCustomerOrders(saleYear);

		// TODO generate the HTML using a template
		model.addAttribute("appName", exportConfiguration.getAppName());
		model.addAttribute("saleYear", saleYear);
		model.addAttribute("orders", orders);
		model.addAttribute("plants", plants);

		// use the orders template
		return "orders";
	}
}
