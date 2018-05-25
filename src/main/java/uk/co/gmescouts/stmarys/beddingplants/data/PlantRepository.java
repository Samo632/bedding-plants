package uk.co.gmescouts.stmarys.beddingplants.data;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;

public interface PlantRepository extends JpaRepository<Plant, Long> {
	Plant findByNum(Integer num);

	Plant findByName(String name);
}
