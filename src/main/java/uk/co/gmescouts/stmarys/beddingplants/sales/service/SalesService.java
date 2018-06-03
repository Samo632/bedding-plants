package uk.co.gmescouts.stmarys.beddingplants.sales.service;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import uk.co.gmescouts.stmarys.beddingplants.data.OrderRepository;
import uk.co.gmescouts.stmarys.beddingplants.data.PlantRepository;
import uk.co.gmescouts.stmarys.beddingplants.data.SaleRepository;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Sale;

@Service
public class SalesService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SalesService.class);

	@Resource
	private SaleRepository saleRepository;

	@Resource
	private OrderRepository orderRepository;

	@Resource
	private PlantRepository plantRepository;

	public Sale saveSale(final Sale sale) {
		LOGGER.debug("Saving Sale [{}]", sale.getYear());

		return saleRepository.save(sale);
	}

	public Boolean deleteSale(@NotNull final Integer year) {
		LOGGER.info("Deleting Sale [{}]", year);

		// first check if there is a matching Sale
		final Sale sale = saleRepository.findByYear(year);

		// delete it
		boolean deleted = false;
		if (sale != null) {
			saleRepository.delete(sale);
			deleted = true;
		}

		return deleted;
	}

	public Sale findSaleByYear(@NotNull final Integer year) {
		LOGGER.info("Finding Sale by Year [{}]", year);

		return saleRepository.findByYear(year);
	}

	public Set<Sale> findAllSales() {
		LOGGER.info("Finding all Sales");

		// find and sort by Year
		return new HashSet<>(saleRepository.findAll(Sort.by(Sort.Order.asc("year"))));
	}

	public Order findOrderByNumAndSaleYear(@NotNull final Integer orderNumber, @NotNull final Integer saleYear) {
		LOGGER.info("Finding Order [{}] for Sale [{}]", orderNumber, saleYear);

		return orderRepository.findByNumAndCustomerSaleYear(orderNumber, saleYear);
	}

	public Plant findPlantByNumAndSaleYear(@NotNull final Integer plantNumber, @NotNull final Integer saleYear) {
		LOGGER.info("Finding Plant [{}] for Sale [{}]", plantNumber, saleYear);

		return plantRepository.findByNumAndSaleYear(plantNumber, saleYear);
	}

	// TODO: Delete Sale(s)
	// TODO: Delete Order(s)
	// TODO: Delete Plant(s)
}
