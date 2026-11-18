package com.simpleaccounts.service.impl;

import com.simpleaccounts.dao.CurrencyDao;
import com.simpleaccounts.entity.Currency;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CurrencyCatalogSeeder implements ApplicationRunner {

	private final CurrencyDao currencyDao;

	public CurrencyCatalogSeeder(CurrencyDao currencyDao) {
		this.currencyDao = currencyDao;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		List<Currency> existingCurrencies = currencyDao.getCurrenciesProfile();
		Set<String> existingCodes = new HashSet<>();
		int orderSequence = 0;
		for (Currency currency : existingCurrencies) {
			if (currency.getCurrencyIsoCode() != null) {
				existingCodes.add(currency.getCurrencyIsoCode().toUpperCase(Locale.ROOT));
			}
			if (currency.getOrderSequence() != null) {
				orderSequence = Math.max(orderSequence, currency.getOrderSequence());
			}
		}

		List<java.util.Currency> availableCurrencies = new ArrayList<>(
				java.util.Currency.getAvailableCurrencies());
		availableCurrencies.sort(Comparator.comparing(java.util.Currency::getCurrencyCode));

		for (java.util.Currency availableCurrency : availableCurrencies) {
			String code = availableCurrency.getCurrencyCode();
			if (!code.matches("[A-Z]{3}") || !existingCodes.add(code)) {
				continue;
			}

			Currency currency = new Currency();
			currency.setCurrencyName(availableCurrency.getDisplayName(Locale.ENGLISH));
			currency.setCurrencyDescription(availableCurrency.getDisplayName(Locale.ENGLISH));
			currency.setCurrencyIsoCode(code);
			currency.setCurrencySymbol(availableCurrency.getSymbol(Locale.US));
			currency.setDefaultFlag('N');
			currency.setOrderSequence(++orderSequence);
			currency.setCreatedBy(0);
			currency.setDeleteFlag(Boolean.FALSE);
			currency.setVersionNumber(1);
			currencyDao.persist(currency);
		}
	}
}
