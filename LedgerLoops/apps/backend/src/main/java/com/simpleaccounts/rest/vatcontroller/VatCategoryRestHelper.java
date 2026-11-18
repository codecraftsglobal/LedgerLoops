package com.simpleaccounts.rest.vatcontroller;

import com.simpleaccounts.entity.Company;
import com.simpleaccounts.entity.Country;
import com.simpleaccounts.entity.State;
import com.simpleaccounts.entity.VatCategory;
import com.simpleaccounts.service.CompanyService;
import com.simpleaccounts.service.VatCategoryService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class VatCategoryRestHelper {

	private final VatCategoryService vatCategoryService;

	private final CompanyService companyService;

	public VatCategory getEntity(VatCategoryRequestModel vatCatRequestModel) {

		if (vatCatRequestModel != null) {

			VatCategory vatCategory = new VatCategory();

			if (vatCatRequestModel.getId() != null) {
				vatCategory = vatCategoryService.findByPK(vatCatRequestModel.getId());
			}

			vatCategory.setName(vatCatRequestModel.getName());
			vatCategory.setVat(vatCatRequestModel.getVat());
			vatCategory.setTaxType(defaultValue(vatCatRequestModel.getTaxType(), "SALES_TAX")
					.toUpperCase(Locale.ROOT));
			vatCategory.setTaxCode(defaultValue(vatCatRequestModel.getTaxCode(),
					toTaxCode(vatCatRequestModel.getName())).toUpperCase(Locale.ROOT));
			String countryIsoCode = emptyToNull(vatCatRequestModel.getCountryIsoCode());
			vatCategory.setCountryIsoCode(countryIsoCode == null ? null : countryIsoCode.toUpperCase(Locale.ROOT));
			vatCategory.setRegionCode(emptyToNull(vatCatRequestModel.getRegionCode()));
			vatCategory.setEffectiveFrom(vatCatRequestModel.getEffectiveFrom());
			vatCategory.setEffectiveTo(vatCatRequestModel.getEffectiveTo());
			vatCategory.setCalculationMode(defaultValue(vatCatRequestModel.getCalculationMode(), "FLAT_RATE")
					.toUpperCase(Locale.ROOT));
			vatCategory.setMinimumAmount(vatCatRequestModel.getMinimumAmount());
			vatCategory.setMaximumAmount(vatCatRequestModel.getMaximumAmount());

			return vatCategory;
		}

		return null;
	}

	@Transactional(readOnly = true)
	public List<VatCategoryModel> getList(Object vatCategories) {
		return getList(vatCategories, false);
	}

	@Transactional(readOnly = true)
	public List<VatCategoryModel> getList(Object vatCategories, boolean includeAllTypes) {

		List<VatCategoryModel> vatCatModelList = new ArrayList<>();
		Company company = companyService.getCompany();
		if (vatCategories != null) {

			for (VatCategory vatCategory : (List<VatCategory>) vatCategories) {
				VatCategoryModel vatCatModel = new VatCategoryModel();

					if (vatCategory.getId() != 10
							&& isApplicableToCompany(vatCategory, company)
							&& (includeAllTypes || "SALES_TAX".equalsIgnoreCase(defaultValue(
									vatCategory.getTaxType(), "SALES_TAX")))
							&& (includeAllTypes || isEffectiveToday(vatCategory))) {
						vatCatModel.setId(vatCategory.getId());
						vatCatModel.setVat(vatCategory.getVat());
						vatCatModel.setName(vatCategory.getName());
						vatCatModel.setTaxType(defaultValue(vatCategory.getTaxType(), "SALES_TAX"));
						vatCatModel.setTaxCode(defaultValue(vatCategory.getTaxCode(), toTaxCode(vatCategory.getName())));
						vatCatModel.setCountryIsoCode(vatCategory.getCountryIsoCode());
						vatCatModel.setRegionCode(vatCategory.getRegionCode());
						vatCatModel.setEffectiveFrom(vatCategory.getEffectiveFrom());
						vatCatModel.setEffectiveTo(vatCategory.getEffectiveTo());
						vatCatModel.setCalculationMode(defaultValue(vatCategory.getCalculationMode(), "FLAT_RATE"));
						vatCatModel.setMinimumAmount(vatCategory.getMinimumAmount());
						vatCatModel.setMaximumAmount(vatCategory.getMaximumAmount());

					vatCatModelList.add(vatCatModel);

				}
			}
			if (company != null && company.getIsRegisteredVat() != null
					&& !company.getIsRegisteredVat() && !includeAllTypes) {
				VatCategoryModel vatCatModel = new VatCategoryModel();
				vatCatModel.setId(10);
				vatCatModel.setVat(BigDecimal.valueOf(0));
				vatCatModel.setName("N/A");
				vatCatModelList.add(vatCatModel);
			}
		}

		return vatCatModelList;
	}

	public VatCategoryModel getModel(VatCategory vatCategory) {

		if (vatCategory != null) {
			VatCategoryModel vatCatModel = new VatCategoryModel();

			vatCatModel.setId(vatCategory.getId());
			vatCatModel.setVat(vatCategory.getVat());
			vatCatModel.setName(vatCategory.getName());
			vatCatModel.setTaxType(defaultValue(vatCategory.getTaxType(), "SALES_TAX"));
			vatCatModel.setTaxCode(defaultValue(vatCategory.getTaxCode(), toTaxCode(vatCategory.getName())));
			vatCatModel.setCountryIsoCode(vatCategory.getCountryIsoCode());
			vatCatModel.setRegionCode(vatCategory.getRegionCode());
			vatCatModel.setEffectiveFrom(vatCategory.getEffectiveFrom());
			vatCatModel.setEffectiveTo(vatCategory.getEffectiveTo());
			vatCatModel.setCalculationMode(defaultValue(vatCategory.getCalculationMode(), "FLAT_RATE"));
			vatCatModel.setMinimumAmount(vatCategory.getMinimumAmount());
			vatCatModel.setMaximumAmount(vatCategory.getMaximumAmount());

			return vatCatModel;
		}

		return null;
	}

	private String toTaxCode(String value) {
		if (value == null) {
			return null;
		}
		return value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
	}

	private String defaultValue(String value, String defaultValue) {
		return value == null || value.isBlank() ? defaultValue : value.trim();
	}

	private String emptyToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private boolean isApplicableToCompany(VatCategory rate, Company company) {
		if (company == null) {
			return rate.getCountryIsoCode() == null && rate.getRegionCode() == null;
		}
		Country country = company.getCompanyCountryCode();
		String countryIsoCode = country == null ? null : country.getIsoAlpha3Code();
		if (rate.getCountryIsoCode() != null
				&& (countryIsoCode == null || !rate.getCountryIsoCode().equalsIgnoreCase(countryIsoCode))) {
			return false;
		}
		State state = company.getCompanyStateCode();
		String regionCode = state == null ? null : state.getStateName();
		return rate.getRegionCode() == null
				|| (regionCode != null && rate.getRegionCode().equalsIgnoreCase(regionCode));
	}

	private boolean isEffectiveToday(VatCategory rate) {
		LocalDate today = LocalDate.now();
		return (rate.getEffectiveFrom() == null || !today.isBefore(rate.getEffectiveFrom()))
				&& (rate.getEffectiveTo() == null || !today.isAfter(rate.getEffectiveTo()));
	}

}
