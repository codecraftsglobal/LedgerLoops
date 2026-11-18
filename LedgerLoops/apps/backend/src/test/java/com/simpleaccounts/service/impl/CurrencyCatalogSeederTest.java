package com.simpleaccounts.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.simpleaccounts.dao.CurrencyDao;
import com.simpleaccounts.entity.Currency;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

@ExtendWith(MockitoExtension.class)
class CurrencyCatalogSeederTest {

	@Mock
	private CurrencyDao currencyDao;

	@Mock
	private ApplicationArguments applicationArguments;

	@Test
	void addsMissingIsoCurrenciesWithoutReplacingExistingCodes() {
		Currency existingPkr = new Currency();
		existingPkr.setCurrencyIsoCode("PKR");
		existingPkr.setOrderSequence(107);
		when(currencyDao.getCurrenciesProfile()).thenReturn(List.of(existingPkr));

		new CurrencyCatalogSeeder(currencyDao).run(applicationArguments);

		ArgumentCaptor<Currency> captor = ArgumentCaptor.forClass(Currency.class);
		verify(currencyDao, org.mockito.Mockito.atLeastOnce()).persist(captor.capture());
		List<Currency> inserted = captor.getAllValues();
		Set<String> insertedCodes = new HashSet<>();
		for (Currency currency : inserted) {
			assertTrue(insertedCodes.add(currency.getCurrencyIsoCode()));
		}
		assertTrue(insertedCodes.contains("USD"));
		assertFalse(insertedCodes.contains("PKR"));
		verify(currencyDao, never()).updateCurrency(107);
	}
}
