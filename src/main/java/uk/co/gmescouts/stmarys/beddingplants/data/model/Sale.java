package uk.co.gmescouts.stmarys.beddingplants.data.model;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "sales")
@Data
@Builder
@EqualsAndHashCode(of = "year")
@NoArgsConstructor
@AllArgsConstructor
public class Sale {
	@Id
	@NonNull
	private Integer year;

	@NonNull
	@Min(0)
	private Float vat;

	@NonNull
	@Builder.Default
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "sale")
	private Set<Order> orders = new TreeSet<>(Comparator.comparingInt(Order::getNum));

	@NonNull
	@Builder.Default
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "sale")
	private Set<Plant> plants = new TreeSet<>(Comparator.comparingInt(Plant::getNum));

	public void addOrder(final Order order) {
		if (order != null) {
			orders.add(order);
			order.setSale(this);
		}
	}

	public void addPlant(final Plant plant) {
		if (plant != null) {
			plants.add(plant);
			plant.setSale(this);
		}
	}
}
