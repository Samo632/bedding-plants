package uk.co.gmescouts.stmarys.beddingplants.data;

import java.util.Set;

import javax.persistence.OrderBy;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.DeliveryRoute;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Sale;

public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, Long> {
	@OrderBy("num")
	Set<DeliveryRoute> findDeliveryRouteBySale(Sale sale);

	DeliveryRoute findDeliveryRouteByOrder(Order order);
}
