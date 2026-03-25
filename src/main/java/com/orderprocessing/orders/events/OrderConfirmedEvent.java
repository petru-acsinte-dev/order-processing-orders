package com.orderprocessing.orders.events;

import java.util.UUID;

/**
 * Event used to announce the confirmation of an order.
 */
public record OrderConfirmedEvent(UUID orderExternalId) {}