package com.simpleaccounts.rest.vatcontroller;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class VatCategoryModel {

	private Integer id;
	private BigDecimal vat;
	private String name;
	private String taxType;
	private String taxCode;
	private String countryIsoCode;
	private String regionCode;
	private LocalDate effectiveFrom;
	private LocalDate effectiveTo;
	private String calculationMode;
	private BigDecimal minimumAmount;
	private BigDecimal maximumAmount;
}
