package com.orderprocessing.orders.constants;

public enum Status {

	CREATED( (short) 0),
	CANCELLED( (short) 1),
	SHIPPED( (short) 2),
	CONFIRMED( (short) 3);

	private final short id;

	private Status(short id) {
		this.id = id;
	}

	public short getId() {
		return id;
	}

}
