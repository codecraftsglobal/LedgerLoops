package com.simpleaccounts.rest.vatcontroller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import lombok.Data;

@Data
public class VatCategoryRequestModel {

	private Integer id;
	private String name;
	private BigDecimal vat;
	private String taxType;
	private String taxCode;
	private String countryIsoCode;
	private String regionCode;
	private LocalDate effectiveFrom;
	private LocalDate effectiveTo;
	private String calculationMode;
	private BigDecimal minimumAmount;
	private BigDecimal maximumAmount;
	private Character defaultFlag;
	private Integer orderSequence;
	private Integer createdBy = 0;
	private Date createdDate;
	private Integer lastUpdateBy;
	private Date lastUpdateDate;
	private Boolean deleteFlag = Boolean.FALSE;
	private Integer versionNumber = 1;
}
