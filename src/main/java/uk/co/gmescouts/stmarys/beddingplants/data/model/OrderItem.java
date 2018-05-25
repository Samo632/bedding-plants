package uk.co.gmescouts.stmarys.beddingplants.data.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "orderitems")
@Data
@Builder
@EqualsAndHashCode(exclude = { "count" })
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
	@JsonIgnore
	@Id
	public Long getId() {
		// key on the Sale year, Order num and Plant num
		return Long.valueOf(String.format("%d%02d%03d", order.getSale().getYear(), plant.getNum(), order.getNum()));
	}

	public void setId(final Long id) {
		// intentionally blank, only needed for Hibernate
	}

	@JsonIgnore
	@Access(AccessType.FIELD)
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	private Order order;

	@NonNull
	@Access(AccessType.FIELD)
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	private Plant plant;

	@NonNull
	@Min(1)
	private Integer count;
}
