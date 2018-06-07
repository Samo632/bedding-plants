package uk.co.gmescouts.stmarys.beddingplants.exports.service;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import uk.co.gmescouts.stmarys.beddingplants.data.OrderRepository;
import uk.co.gmescouts.stmarys.beddingplants.data.PlantRepository;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;

@Service
public class ExportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExportService.class);

	@Resource
	private OrderRepository orderRepository;

	@Resource
	private PlantRepository plantRepository;

	public Set<Order> getSaleCustomerOrders(@NotNull final Integer saleYear) {
		LOGGER.info("Get Customer Orders for Sale [{}]", saleYear);

		return orderRepository.findByCustomerSaleYear(saleYear);
	}

	public Set<Plant> getSalePlants(@NotNull final Integer saleYear) {
		LOGGER.info("Get Plants for Sale [{}]", saleYear);

		return plantRepository.findBySaleYear(saleYear);
	}

	public byte[] exportSaleCustomersToPdf(@NotNull final Integer saleYear) {
		LOGGER.info("Exporting Customer Orders for Sale [{}]", saleYear);

		// get the Orders
		final Set<Order> orders = getSaleCustomerOrders(saleYear);

		// Apply preferences and build metadata.
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PdfWriter writer = new PdfWriter(baos);
		final PdfDocument document = new PdfDocument(writer);
		document.setDefaultPageSize(PageSize.A4);
		// TODO: writer.setViewerPreferences();

		// Build PDF Document
		// document.open();

		// TODO: page header

		// process Orders (in Number order)
		orders.forEach(order -> {
			// TODO: output details for each order
		});

		// complete the PDF Document
		document.close();

		return baos.toByteArray();
	}
}
