package uk.co.gmescouts.stmarys.beddingplants.data;

import java.util.Set;

import javax.persistence.OrderBy;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Sale;

public interface PlantRepository extends JpaRepository<Plant, Long> {
	Plant findByNumAndSaleYear(Integer num, Integer saleYear);

	@OrderBy("num")
	Set<Plant> findBySaleYear(Integer saleYear);
}
