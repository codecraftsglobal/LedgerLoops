package com.simpleaccounts.rest.taxescontroller;

import com.simpleaccounts.entity.Company;
import com.simpleaccounts.entity.Country;
import com.simpleaccounts.entity.State;
import com.simpleaccounts.entity.VatCategory;
import com.simpleaccounts.rest.vatcontroller.VatCategoryModel;
import com.simpleaccounts.security.JwtTokenUtil;
import com.simpleaccounts.service.CompanyService;
import com.simpleaccounts.service.VatCategoryService;
import com.simpleaccounts.service.impl.TaxRateCalculator;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/rest/taxes")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaxRateController {

	private final VatCategoryService vatCategoryService;
	private final CompanyService companyService;
	private final JwtTokenUtil jwtTokenUtil;
	private final TaxRateCalculator taxRateCalculator;

	@GetMapping("/getTaxRates")
	public ResponseEntity<List<VatCategoryModel>> getTaxRates(
			@RequestParam(required = false) String taxType,
			HttpServletRequest request) {
		jwtTokenUtil.getUserIdFromHttpRequest(request);
		Company company = companyService.getCompany();
		String countryIsoCode = getCountryIsoCode(company);
		String regionCode = getRegionCode(company);

		List<VatCategoryModel> rates = vatCategoryService.getVatCategoryList().stream()
				.filter(rate -> !Boolean.TRUE.equals(rate.getDeleteFlag()))
				.filter(rate -> isBlank(taxType) || "ALL".equalsIgnoreCase(taxType)
						|| taxType.equalsIgnoreCase(rate.getTaxType()))
				.filter(rate -> isBlank(rate.getCountryIsoCode())
						|| rate.getCountryIsoCode().equalsIgnoreCase(countryIsoCode))
				.filter(rate -> isBlank(rate.getRegionCode())
						|| rate.getRegionCode().equalsIgnoreCase(regionCode))
				.sorted(Comparator.comparing(VatCategory::getTaxType,
								Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER))
						.thenComparing(VatCategory::getTaxCode,
								Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER))
						.thenComparing(VatCategory::getEffectiveFrom,
								Comparator.nullsFirst(Comparator.naturalOrder())))
				.map(this::toModel)
				.toList();
		return ResponseEntity.ok(rates);
	}

	@PostMapping("/calculate")
	public ResponseEntity<?> calculate(
			@RequestBody TaxCalculationRequest request,
			HttpServletRequest httpRequest) {
		jwtTokenUtil.getUserIdFromHttpRequest(httpRequest);
		if (request == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Tax calculation details are required."));
		}
		Company company = companyService.getCompany();
		if (isBlank(request.getCountryIsoCode())) {
			request.setCountryIsoCode(getCountryIsoCode(company));
		}
		if (isBlank(request.getRegionCode())) {
			request.setRegionCode(getRegionCode(company));
		}

		try {
			return ResponseEntity.ok(taxRateCalculator.calculate(request));
		} catch (IllegalArgumentException exception) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", exception.getMessage()));
		}
	}

	private VatCategoryModel toModel(VatCategory rate) {
		VatCategoryModel model = new VatCategoryModel();
		model.setId(rate.getId());
		model.setName(rate.getName());
		model.setVat(rate.getVat());
		model.setTaxType(rate.getTaxType());
		model.setTaxCode(rate.getTaxCode());
		model.setCountryIsoCode(rate.getCountryIsoCode());
		model.setRegionCode(rate.getRegionCode());
		model.setEffectiveFrom(rate.getEffectiveFrom());
		model.setEffectiveTo(rate.getEffectiveTo());
		model.setCalculationMode(rate.getCalculationMode());
		model.setMinimumAmount(rate.getMinimumAmount());
		model.setMaximumAmount(rate.getMaximumAmount());
		return model;
	}

	private String getCountryIsoCode(Company company) {
		Country country = company == null ? null : company.getCompanyCountryCode();
		return country == null ? null : country.getIsoAlpha3Code();
	}

	private String getRegionCode(Company company) {
		State state = company == null ? null : company.getCompanyStateCode();
		return state == null ? null : state.getStateName();
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
