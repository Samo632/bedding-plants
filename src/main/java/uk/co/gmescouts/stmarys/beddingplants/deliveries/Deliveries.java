package uk.co.gmescouts.stmarys.beddingplants.deliveries;

import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.co.gmescouts.stmarys.beddingplants.data.model.DeliveryRoute;
import uk.co.gmescouts.stmarys.beddingplants.deliveries.service.DeliveriesService;

@RestController
@RequestMapping(value = "/deliveries")
public class Deliveries {
	private static final Logger LOGGER = LoggerFactory.getLogger(Deliveries.class);

	// Delivery Routes
	private static final String DELIVERY_ROUTES = "/routes/{saleYear}";

	@Resource
	private DeliveriesService deliveriesService;

	@GetMapping(DELIVERY_ROUTES)
	public Set<DeliveryRoute> getDeliveryRoutes(@PathVariable final Integer saleYear) {
		LOGGER.info("Retrieving Delivery Routes for Sale Year [{}]", saleYear);

		return deliveriesService.getDeliveryRoutes(saleYear);
	}
}
