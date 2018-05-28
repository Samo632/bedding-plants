package uk.co.gmescouts.stmarys.beddingplants.data.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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
@Table(name = "plants")
@Data
@Builder
@EqualsAndHashCode(of = { "sale", "num" })
@NoArgsConstructor
@AllArgsConstructor
public class Plant {
	@JsonIgnore
	@Id
	public Long getId() {
		// key on the Sale year and Plant num
		return Long.valueOf(String.format("%d%02d", sale.getYear(), this.num));
	}

	public void setId(final Long id) {
		// intentionally blank, only needed for Hibernate
	}

	@JsonIgnore
	@Access(AccessType.FIELD)
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	private Sale sale;

	@NonNull
	@Min(1)
	@Column(nullable = false)
	private Integer num;

	@NonNull
	@Column(nullable = false)
	private String name;

	@NonNull
	@Column(nullable = false)
	private String variety;

	private String details;

	@NonNull
	@Min(0)
	@Column(nullable = false)
	private Float price;

	@NonNull
	@Min(0)
	@Column(nullable = false)
	private Float cost;
}
