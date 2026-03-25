package com.orderprocessing.orders.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.orderprocessing.common.filters.JWTValidatorCommon;

@Service
/**
 * Bean that extracts token info.
 * Holds secret key defined in application.properties.
 */
public class JWTService extends JWTValidatorCommon {

	public JWTService(@Value("${jwt.secret}") String secret) {
		super(secret);
	}

}
