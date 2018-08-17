package uk.co.gmescouts.stmarys.beddingplants.data;

import java.util.Set;

import javax.persistence.OrderBy;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.DeliveryRoute;

public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, Long> {
	@OrderBy("num")
	Set<DeliveryRoute> findDeliveryRouteBySaleYear(Integer saleYear);

	DeliveryRoute findDeliveryRouteBySaleYearAndOrdersNum(Integer saleYear, Integer orderNum);
}
