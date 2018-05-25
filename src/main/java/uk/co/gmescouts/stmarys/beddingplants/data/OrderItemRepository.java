package uk.co.gmescouts.stmarys.beddingplants.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderItem;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
	List<OrderItem> findByOrder(Order order);

	List<OrderItem> findByPlant(Plant plant);
}
