package uk.co.gmescouts.stmarys.beddingplants.data;

import java.util.Set;

import javax.persistence.OrderBy;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.DeliveryDay;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderType;

public interface OrderRepository extends JpaRepository<Order, Long> {
	Order findByNumAndCustomerSaleYear(Integer num, Integer customerSaleYear);

	@OrderBy("num")
	Set<Order> findByDeliveryDayAndCustomerSaleYear(DeliveryDay deliveryDay, Integer customerSaleYear);

	@OrderBy("num")
	Set<Order> findByOrderTypeAndCustomerSaleYear(OrderType orderType, Integer customerSaleYear);

	@OrderBy("num")
	Set<Order> findByCustomerSaleYear(Integer customerSaleYear);
}
