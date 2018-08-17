package uk.co.gmescouts.stmarys.beddingplants.deliveries.service;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import uk.co.gmescouts.stmarys.beddingplants.data.model.DeliveryRoute;
import uk.co.gmescouts.stmarys.beddingplants.deliveries.configuration.DeliveriesConfiguration;

@Service
public class DeliveriesService {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeliveriesService.class);

	@Resource
	private DeliveriesConfiguration deliveriearConfiguration;

	public Set<DeliveryRoute> getDeliveryRoutes(@NotNull final Integer saleYear) {
		LOGGER.info("Retrieving Delivery Routes for Sale Year [{}]", saleYear);

		final Set<DeliveryRoute> deliveryRoutes = Collections.emptySet();

		// FIXME: some logic, d'uh...

		return deliveryRoutes;
	}
}
