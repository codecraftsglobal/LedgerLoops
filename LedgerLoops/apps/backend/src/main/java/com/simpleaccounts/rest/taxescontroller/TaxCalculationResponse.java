package com.simpleaccounts.rest.taxescontroller;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class TaxCalculationResponse {

	private String taxType;
	private String taxCode;
	private String countryIsoCode;
	private String regionCode;
	private LocalDate taxDate;
	private BigDecimal taxableAmount;
	private BigDecimal taxAmount;
	private BigDecimal effectiveRate;
}
