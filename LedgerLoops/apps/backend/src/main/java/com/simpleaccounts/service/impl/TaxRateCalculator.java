package com.simpleaccounts.service.impl;

import com.simpleaccounts.entity.VatCategory;
import com.simpleaccounts.rest.taxescontroller.TaxCalculationRequest;
import com.simpleaccounts.rest.taxescontroller.TaxCalculationResponse;
import com.simpleaccounts.service.VatCategoryService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class TaxRateCalculator {

	private static final String FLAT_RATE = "FLAT_RATE";
	private static final String PROGRESSIVE_BANDS = "PROGRESSIVE_BANDS";
	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

	private final VatCategoryService vatCategoryService;

	public TaxRateCalculator(VatCategoryService vatCategoryService) {
		this.vatCategoryService = vatCategoryService;
	}

	public TaxCalculationResponse calculate(TaxCalculationRequest request) {
		validateRequest(request);
		String taxType = normalize(request.getTaxType());
		String taxCode = normalize(request.getTaxCode());
		String countryIsoCode = normalize(request.getCountryIsoCode());
		String regionCode = normalize(request.getRegionCode());
		LocalDate taxDate = request.getTaxDate();
		BigDecimal taxableAmount = request.getTaxableAmount().setScale(2, RoundingMode.HALF_UP);

		List<VatCategory> matchingRates = vatCategoryService.getVatCategoryList().stream()
				.filter(rate -> !Boolean.TRUE.equals(rate.getDeleteFlag()))
				.filter(rate -> taxType.equals(normalize(rate.getTaxType())))
				.filter(rate -> taxCode.equals(normalize(rate.getTaxCode())))
				.filter(rate -> isInEffect(rate, taxDate))
				.filter(rate -> matchesCountry(rate, countryIsoCode))
				.filter(rate -> matchesRegion(rate, regionCode))
				.toList();

		if (matchingRates.isEmpty()) {
			throw new IllegalArgumentException("No tax rate is configured for this date and jurisdiction.");
		}

		int specificity = matchingRates.stream()
				.mapToInt(rate -> specificity(rate, countryIsoCode, regionCode))
				.max()
				.orElse(0);
		List<VatCategory> applicableRates = matchingRates.stream()
				.filter(rate -> specificity(rate, countryIsoCode, regionCode) == specificity)
				.sorted(Comparator.comparing(VatCategory::getMinimumAmount,
						Comparator.nullsFirst(Comparator.naturalOrder())))
				.toList();

		BigDecimal taxAmount;
		if (PROGRESSIVE_BANDS.equals(normalize(applicableRates.get(0).getCalculationMode()))) {
			if (applicableRates.stream().anyMatch(rate ->
					!PROGRESSIVE_BANDS.equals(normalize(rate.getCalculationMode())))) {
				throw new IllegalArgumentException("Tax bands for this code must use one calculation mode.");
			}
			taxAmount = calculateProgressive(applicableRates, taxableAmount);
		} else {
			if (applicableRates.size() != 1
					|| !FLAT_RATE.equals(normalize(applicableRates.get(0).getCalculationMode()))) {
				throw new IllegalArgumentException("Multiple flat tax rates are active for this code.");
			}
			taxAmount = calculateAtRate(taxableAmount, applicableRates.get(0).getVat())
					.setScale(2, RoundingMode.HALF_UP);
		}

		TaxCalculationResponse response = new TaxCalculationResponse();
		response.setTaxType(taxType);
		response.setTaxCode(taxCode);
		response.setCountryIsoCode(countryIsoCode);
		response.setRegionCode(regionCode);
		response.setTaxDate(taxDate);
		response.setTaxableAmount(taxableAmount);
		response.setTaxAmount(taxAmount);
		response.setEffectiveRate(taxableAmount.signum() == 0
				? BigDecimal.ZERO.setScale(4)
				: taxAmount.multiply(ONE_HUNDRED).divide(taxableAmount, 4, RoundingMode.HALF_UP));
		return response;
	}

	private void validateRequest(TaxCalculationRequest request) {
		if (request == null || isBlank(request.getTaxType()) || isBlank(request.getTaxCode())) {
			throw new IllegalArgumentException("Tax type and tax code are required.");
		}
		if (request.getTaxDate() == null) {
			throw new IllegalArgumentException("Tax date is required.");
		}
		if (request.getTaxableAmount() == null || request.getTaxableAmount().signum() < 0) {
			throw new IllegalArgumentException("Taxable amount must be zero or greater.");
		}
	}

	private BigDecimal calculateProgressive(List<VatCategory> bands, BigDecimal taxableAmount) {
		if (taxableAmount.signum() == 0) {
			return BigDecimal.ZERO.setScale(2);
		}

		BigDecimal tax = BigDecimal.ZERO;
		BigDecimal previousUpperBound = BigDecimal.ZERO;
		for (VatCategory band : bands) {
			BigDecimal lowerBound = band.getMinimumAmount() == null
					? BigDecimal.ZERO
					: band.getMinimumAmount();
			BigDecimal upperBound = band.getMaximumAmount();

			if (lowerBound.compareTo(previousUpperBound) < 0) {
				throw new IllegalArgumentException("Progressive tax bands overlap.");
			}
			if (taxableAmount.compareTo(lowerBound) > 0
					&& lowerBound.compareTo(previousUpperBound) > 0) {
				throw new IllegalArgumentException("Progressive tax bands contain a gap.");
			}
			if (upperBound != null && upperBound.compareTo(lowerBound) <= 0) {
				throw new IllegalArgumentException("Each tax band maximum must exceed its minimum.");
			}
			if (taxableAmount.compareTo(lowerBound) <= 0) {
				break;
			}

			BigDecimal taxableInBand = upperBound == null
					? taxableAmount.subtract(lowerBound)
					: taxableAmount.min(upperBound).subtract(lowerBound);
			if (taxableInBand.signum() > 0) {
				tax = tax.add(calculateAtRate(taxableInBand, band.getVat()));
			}

			if (upperBound == null || taxableAmount.compareTo(upperBound) <= 0) {
				previousUpperBound = taxableAmount;
				break;
			}
			previousUpperBound = upperBound;
		}

		if (taxableAmount.compareTo(previousUpperBound) > 0) {
			throw new IllegalArgumentException("Configured tax bands do not cover the taxable amount.");
		}
		return tax.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateAtRate(BigDecimal amount, BigDecimal rate) {
		if (rate == null || rate.signum() < 0 || rate.compareTo(ONE_HUNDRED) > 0) {
			throw new IllegalArgumentException("Configured tax rate must be between 0 and 100 percent.");
		}
		return amount.multiply(rate).divide(ONE_HUNDRED, 10, RoundingMode.HALF_UP);
	}

	private boolean isInEffect(VatCategory rate, LocalDate taxDate) {
		return (rate.getEffectiveFrom() == null || !taxDate.isBefore(rate.getEffectiveFrom()))
				&& (rate.getEffectiveTo() == null || !taxDate.isAfter(rate.getEffectiveTo()));
	}

	private boolean matchesCountry(VatCategory rate, String countryIsoCode) {
		return isBlank(rate.getCountryIsoCode())
				|| (!isBlank(countryIsoCode) && countryIsoCode.equals(normalize(rate.getCountryIsoCode())));
	}

	private boolean matchesRegion(VatCategory rate, String regionCode) {
		return isBlank(rate.getRegionCode())
				|| (!isBlank(regionCode) && regionCode.equals(normalize(rate.getRegionCode())));
	}

	private int specificity(VatCategory rate, String countryIsoCode, String regionCode) {
		if (!isBlank(rate.getRegionCode()) && regionCode != null) {
			return 2;
		}
		return !isBlank(rate.getCountryIsoCode()) && countryIsoCode != null ? 1 : 0;
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
