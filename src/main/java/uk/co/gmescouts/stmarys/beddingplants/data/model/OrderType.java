package uk.co.gmescouts.stmarys.beddingplants.data.model;

import java.util.Arrays;

public enum OrderType {
	Collect('C'), Deliver('D');

	private final Character type;

	OrderType(final Character type) {
		this.type = type;
	}

	public static OrderType valueOf(final Character type) {
		return Arrays.stream(OrderType.values()).filter(orderType -> orderType.type.equals(type)).findFirst().orElse(null);
	}
}
