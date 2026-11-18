package com.simpleaccounts.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.simpleaccounts.entity.VatCategory;
import com.simpleaccounts.rest.taxescontroller.TaxCalculationRequest;
import com.simpleaccounts.rest.taxescontroller.TaxCalculationResponse;
import com.simpleaccounts.service.VatCategoryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaxRateCalculatorTest {

	@Mock
	private VatCategoryService vatCategoryService;

	private TaxRateCalculator calculator;

	@BeforeEach
	void setUp() {
		calculator = new TaxRateCalculator(vatCategoryService);
	}

	@Test
	void appliesTheMostSpecificRateThatIsEffectiveOnTheTaxDate() {
		VatCategory nationalRate = rate("SALES_TAX", "GOODS", "PAK", null, "18", "FLAT_RATE");
		nationalRate.setEffectiveFrom(LocalDate.of(2025, 7, 1));
		VatCategory provincialRate = rate("SALES_TAX", "GOODS", "PAK", "Punjab", "16", "FLAT_RATE");
		provincialRate.setEffectiveFrom(LocalDate.of(2025, 7, 1));
		when(vatCategoryService.getVatCategoryList()).thenReturn(List.of(nationalRate, provincialRate));

		TaxCalculationResponse response = calculator.calculate(request(
				"SALES_TAX", "GOODS", "PAK", "PUNJAB", LocalDate.of(2025, 8, 1), "1000.00"));

		assertEquals(new BigDecimal("160.00"), response.getTaxAmount());
		assertEquals(new BigDecimal("16.0000"), response.getEffectiveRate());
	}

	@Test
	void computesProgressiveTaxAcrossConfiguredBands() {
		VatCategory firstBand = rate("INCOME_TAX", "PERSONAL_2025", "PAK", null, "0",
				"PROGRESSIVE_BANDS");
		firstBand.setMinimumAmount(new BigDecimal("0"));
		firstBand.setMaximumAmount(new BigDecimal("600000"));
		VatCategory secondBand = rate("INCOME_TAX", "PERSONAL_2025", "PAK", null, "5",
				"PROGRESSIVE_BANDS");
		secondBand.setMinimumAmount(new BigDecimal("600000"));
		secondBand.setMaximumAmount(new BigDecimal("1200000"));
		VatCategory topBand = rate("INCOME_TAX", "PERSONAL_2025", "PAK", null, "15",
				"PROGRESSIVE_BANDS");
		topBand.setMinimumAmount(new BigDecimal("1200000"));
		when(vatCategoryService.getVatCategoryList()).thenReturn(List.of(topBand, firstBand, secondBand));

		TaxCalculationResponse response = calculator.calculate(request(
				"INCOME_TAX", "PERSONAL_2025", "PAK", null, LocalDate.of(2025, 8, 1), "1500000"));

		assertEquals(new BigDecimal("75000.00"), response.getTaxAmount());
		assertEquals(new BigDecimal("5.0000"), response.getEffectiveRate());
	}

	@Test
	void rejectsMissingOrExpiredRates() {
		VatCategory rate = rate("WITHHOLDING", "SUPPLIER", "PAK", null, "5", "FLAT_RATE");
		rate.setEffectiveTo(LocalDate.of(2024, 6, 30));
		when(vatCategoryService.getVatCategoryList()).thenReturn(List.of(rate));

		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(request(
				"WITHHOLDING", "SUPPLIER", "PAK", null, LocalDate.of(2024, 7, 1), "1000")));
	}

	@Test
	void rejectsGapsInProgressiveBandsInsteadOfUnderstatingTax() {
		VatCategory firstBand = rate("INCOME_TAX", "PERSONAL", "PAK", null, "5", "PROGRESSIVE_BANDS");
		firstBand.setMinimumAmount(new BigDecimal("0"));
		firstBand.setMaximumAmount(new BigDecimal("500"));
		VatCategory secondBand = rate("INCOME_TAX", "PERSONAL", "PAK", null, "10", "PROGRESSIVE_BANDS");
		secondBand.setMinimumAmount(new BigDecimal("600"));
		when(vatCategoryService.getVatCategoryList()).thenReturn(List.of(firstBand, secondBand));

		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(request(
				"INCOME_TAX", "PERSONAL", "PAK", null, LocalDate.of(2025, 8, 1), "1000")));
	}

	private TaxCalculationRequest request(String taxType, String taxCode, String country, String region,
			LocalDate date, String amount) {
		TaxCalculationRequest request = new TaxCalculationRequest();
		request.setTaxType(taxType);
		request.setTaxCode(taxCode);
		request.setCountryIsoCode(country);
		request.setRegionCode(region);
		request.setTaxDate(date);
		request.setTaxableAmount(new BigDecimal(amount));
		return request;
	}

	private VatCategory rate(String type, String code, String country, String region, String amount,
			String calculationMode) {
		VatCategory rate = new VatCategory();
		rate.setTaxType(type);
		rate.setTaxCode(code);
		rate.setCountryIsoCode(country);
		rate.setRegionCode(region);
		rate.setVat(new BigDecimal(amount));
		rate.setCalculationMode(calculationMode);
		rate.setDeleteFlag(Boolean.FALSE);
		return rate;
	}
}
