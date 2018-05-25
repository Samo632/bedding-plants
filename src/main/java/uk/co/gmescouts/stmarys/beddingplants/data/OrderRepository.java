package uk.co.gmescouts.stmarys.beddingplants.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Customer;
import uk.co.gmescouts.stmarys.beddingplants.data.model.DeliveryDay;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderType;

public interface OrderRepository extends JpaRepository<Order, Long> {
	Order findByNum(Integer num);

	List<Order> findByDeliveryDay(DeliveryDay deliveryDay);

	List<Order> findByOrderType(OrderType orderType);

	List<Order> findByCustomer(Customer customer);
}
