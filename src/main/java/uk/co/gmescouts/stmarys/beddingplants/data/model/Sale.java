package uk.co.gmescouts.stmarys.beddingplants.data.model;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
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
	@Column(nullable = false, unique = true)
	private Integer year;

	@NonNull
	@Min(0)
	@Column(nullable = false)
	private Float vat;

	@NonNull
	@Builder.Default
	@OrderBy("num")
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "sale")
	private Set<Order> orders = new TreeSet<>(Comparator.comparingInt(Order::getNum));

	@NonNull
	@Builder.Default
	@OrderBy("num")
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "sale")
	private Set<Plant> plants = new TreeSet<>(Comparator.comparingInt(Plant::getNum));

	public void addOrder(final Order order) {
		if (order != null) {
			// replace existing Order, if present
			orders.remove(order);
			orders.add(order);
			order.setSale(this);
		}
	}

	public void addPlant(final Plant plant) {
		if (plant != null) {
			// replace existing Plant, if present
			plants.remove(plant);
			plants.add(plant);
			plant.setSale(this);
		}
	}
}
