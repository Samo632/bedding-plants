package uk.co.gmescouts.stmarys.beddingplants.exports.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.PdfException;

import uk.co.gmescouts.stmarys.beddingplants.data.OrderRepository;
import uk.co.gmescouts.stmarys.beddingplants.data.PlantRepository;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderItem;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderType;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;
import uk.co.gmescouts.stmarys.beddingplants.exports.ExportHtml;

@Service
public class ExportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExportService.class);

	@Resource
	private OrderRepository orderRepository;

	@Resource
	private PlantRepository plantRepository;

	@Resource
	private RestTemplate restTemplate;

	@Value("${server.ssl.enabled:false}")
	private boolean httpsEnabled;

	private final String hostname;

	@Value("${server.port}")
	private int port;

	@Value("${server.servlet.context-path}")
	private String baseUri;

	public ExportService() throws UnknownHostException {
		hostname = InetAddress.getLocalHost().getHostName();
	}

	private String getExportHostUrl() {
		return String.format("%s://%s:%d", httpsEnabled ? "https" : "http", hostname, port);
	}

	public Set<Order> getSaleCustomerOrders(@NotNull final Integer saleYear, final OrderType orderType) {
		LOGGER.info("Get Customer Orders for Sale [{}]", saleYear);

		Set<Order> orders;
		if (orderType == null) {
			orders = orderRepository.findByCustomerSaleYear(saleYear);
		} else {
			orders = orderRepository.findByOrderTypeAndCustomerSaleYear(orderType, saleYear);
		}

		return orders;
	}

	public Set<Plant> getSalePlants(@NotNull final Integer saleYear) {
		LOGGER.info("Get Plants for Sale [{}]", saleYear);

		return plantRepository.findBySaleYear(saleYear);
	}

	public Integer orderPlantCount(@NotNull final Order order) {
		return order.getOrderItems().stream().mapToInt(OrderItem::getCount).sum();
	}

	public Integer orderPlantAmount(@NotNull final Order order, @NotNull final Plant plant) {
		final Optional<OrderItem> orderItem = order.getOrderItems().stream().filter(oi -> oi.getPlant().equals(plant)).findFirst();

		Integer amount = null;
		if (orderItem.isPresent()) {
			amount = orderItem.get().getCount();
		}
		return amount;
	}

	public byte[] exportSaleCustomersToPdf(@NotNull final Integer saleYear, final OrderType orderType) throws IOException {
		LOGGER.info("Exporting Customer Orders for Sale [{}]", saleYear);

		// TODO: better way of doing this?
		// setup the URLs
		final String exportHostUrl = getExportHostUrl();
		final String exportHtmlUrl = String.format("%s%s%s%s", exportHostUrl, baseUri,
				ExportHtml.EXPORT_CUSTOMER_ORDERS_HTML.replace("{saleYear}", saleYear.toString()),
				orderType == null ? "" : String.format("?orderType=%s", orderType.toString()));
		LOGGER.debug("Calling HTML Export URL [{}]", exportHtmlUrl);

		// get the HTML via external call
		final String html = restTemplate.getForObject(exportHtmlUrl, String.class, saleYear);

		// converter properties (image locations)
		final ConverterProperties properties = new ConverterProperties();
		properties.setBaseUri(exportHostUrl);

		// convert HTML to PDF
		byte[] pdf = null;
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			HtmlConverter.convertToPdf(html, baos, properties);
			pdf = baos.toByteArray();
		} catch (final PdfException e) {
			LOGGER.error("Unable to generate PDF from HTML: {}", e.getMessage(), e);
		}

		// return converted PDF (if any)
		return pdf;
	}
}
