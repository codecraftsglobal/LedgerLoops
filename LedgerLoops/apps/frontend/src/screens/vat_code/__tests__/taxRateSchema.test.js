import { describe, expect, it } from 'vitest';

import { normalizeTaxRateForm, taxRateSchema } from '../taxRateSchema';

const validRate = {
  name: 'Pakistan GST',
  vat: '18',
  taxType: 'SALES_TAX',
  taxCode: 'PK_GST_GOODS',
  countryIsoCode: 'PAK',
  regionCode: '',
  effectiveFrom: '2025-07-01',
  effectiveTo: '',
  calculationMode: 'FLAT_RATE',
  minimumAmount: '',
  maximumAmount: '',
};

describe('tax rate form validation', () => {
  it('accepts a country-specific effective-dated sales tax rate', () => {
    expect(taxRateSchema.safeParse(validRate).success).toBe(true);
  });

  it('rejects reversed effective dates and incomplete progressive bands', () => {
    expect(
      taxRateSchema.safeParse({
        ...validRate,
        effectiveTo: '2025-06-30',
        calculationMode: 'PROGRESSIVE_BANDS',
      }).success
    ).toBe(false);
  });

  it('normalizes optional empty fields before submitting to the API', () => {
    const result = normalizeTaxRateForm(validRate);

    expect(result.taxCode).toBe('PK_GST_GOODS');
    expect(result.countryIsoCode).toBe('PAK');
    expect(result.effectiveTo).toBeNull();
    expect(result.minimumAmount).toBeNull();
  });
});
