package uk.co.gmescouts.stmarys.beddingplants.exports;

import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderType;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;
import uk.co.gmescouts.stmarys.beddingplants.exports.service.ExportService;

@Controller
public class ExportHtml {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExportHtml.class);

	private final static String EXPORT_BASE = "/export";

	/*
	 * Orders
	 */
	private final static String EXPORT_CUSTOMER_ORDERS = EXPORT_BASE + "/orders/{saleYear}";
	public final static String EXPORT_CUSTOMER_ORDERS_HTML = EXPORT_CUSTOMER_ORDERS + "/html";

	@Value("${spring.application.name}")
	private String appName;

	@Resource
	private ExportService exportService;

	@GetMapping(EXPORT_CUSTOMER_ORDERS_HTML)
	public String exportSaleCustomerOrdersAsHtml(final Model model, @PathVariable final Integer saleYear,
			@RequestParam(required = false) final OrderType orderType) {
		LOGGER.info("Exporting (HTML) Order details for Sale [{}] with Order Type [{}]", saleYear, orderType);

		// get the Plants
		final Set<Plant> plants = exportService.getSalePlants(saleYear);

		// get the Orders
		final Set<Order> orders = exportService.getSaleCustomerOrders(saleYear, orderType);

		// add data attributes to template Model
		model.addAttribute("appName", appName);
		model.addAttribute("saleYear", saleYear);
		model.addAttribute("orders", orders);
		model.addAttribute("plants", plants);

		// use the orders template
		return "orders";
	}

}
