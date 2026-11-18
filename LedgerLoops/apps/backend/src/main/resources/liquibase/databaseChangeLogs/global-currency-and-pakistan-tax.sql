--liquibase formatted sql

--changeset LedgerLoop:global-currency-and-pakistan-tax-001
ALTER TABLE vat_category
    ADD COLUMN tax_type varchar(32) NOT NULL DEFAULT 'SALES_TAX',
    ADD COLUMN tax_code varchar(64),
    ADD COLUMN country_iso_code varchar(3),
    ADD COLUMN region_code varchar(64),
    ADD COLUMN effective_from date,
    ADD COLUMN effective_to date,
    ADD COLUMN calculation_mode varchar(32) NOT NULL DEFAULT 'FLAT_RATE',
    ADD COLUMN minimum_amount numeric(19, 2),
    ADD COLUMN maximum_amount numeric(19, 2);

UPDATE vat_category
SET tax_code = upper(regexp_replace(name, '[^A-Za-z0-9]+', '_', 'g'))
WHERE tax_code IS NULL;

UPDATE vat_category
SET country_iso_code = 'ARE'
WHERE vat_category_id IN (1, 2, 3, 4)
  AND country_iso_code IS NULL;

ALTER TABLE vat_category
    ADD CONSTRAINT chk_vat_category_tax_type
        CHECK (tax_type IN ('SALES_TAX', 'WITHHOLDING', 'INCOME_TAX')),
    ADD CONSTRAINT chk_vat_category_calculation_mode
        CHECK (calculation_mode IN ('FLAT_RATE', 'PROGRESSIVE_BANDS')),
    ADD CONSTRAINT chk_vat_category_effective_dates
        CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from),
    ADD CONSTRAINT chk_vat_category_tax_band
        CHECK (maximum_amount IS NULL OR minimum_amount IS NULL OR maximum_amount > minimum_amount),
    ADD CONSTRAINT chk_vat_category_nonnegative_tax_band
        CHECK ((minimum_amount IS NULL OR minimum_amount >= 0)
            AND (maximum_amount IS NULL OR maximum_amount >= 0));

CREATE INDEX idx_vat_category_tax_lookup
    ON vat_category (tax_type, tax_code, country_iso_code, region_code, effective_from, effective_to)
    WHERE delete_flag = false;

UPDATE country
SET iso_alpha3_code = 'PAK',
    currency_code = (SELECT currency_code FROM currency WHERE currency_iso_code = 'PKR' LIMIT 1)
WHERE country_code = 166;

UPDATE country
SET iso_alpha3_code = 'ARE',
    currency_code = (SELECT currency_code FROM currency WHERE currency_iso_code = 'AED' LIMIT 1)
WHERE country_code = 229;
