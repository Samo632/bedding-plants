package uk.co.gmescouts.stmarys.beddingplants.data;

import java.util.Set;

import javax.persistence.OrderBy;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Customer;
import uk.co.gmescouts.stmarys.beddingplants.data.model.DeliveryDay;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderType;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Sale;

public interface OrderRepository extends JpaRepository<Order, Long> {
	Order findByNumAndSale(Integer num, Sale sale);

	Order findByNumAndSaleYear(Integer num, Integer saleYear);

	@OrderBy("num")
	Set<Order> findByDeliveryDayAndSale(DeliveryDay deliveryDay, Sale sale);

	@OrderBy("num")
	Set<Order> findByOrderTypeAndSale(OrderType orderType, Sale sale);

	@OrderBy("num")
	Set<Order> findByCustomerAndSale(Customer customer, Sale sale);

	@OrderBy("num")
	Set<Order> findBySale(Sale sale);
}
