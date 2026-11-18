# Global Currencies and Pakistan Tax Configuration

## Currency catalog

On startup, LedgerLoop adds ISO 4217 currency codes available to the running Java
runtime to the existing currency catalog. Existing currency records are preserved.
Company registration defaults to AED for the UAE and PKR for Pakistan; users can
choose another available currency.

The existing accounting model and conversion workflows remain in place. Amounts
are generally stored and calculated to two decimal places, so currencies with
three minor-unit digits may lose precision in some workflows.

## Configure tax rates

Open **Tax Rates** and create a rate for each tax code and jurisdiction:

1. Select the tax type: sales tax/GST, withholding, or income/advance tax.
2. Enter a tax code and percentage.
3. Optionally enter the three-letter ISO country code (for example, `PAK`) and
   the province or jurisdiction name. Leave these blank for a general rate.
4. Set effective dates when the rate is time-limited.
5. For progressive calculations, create bands using the same tax code and
   jurisdiction. Each band needs a minimum, an optional maximum, and its own rate.

The calculator selects rates by tax date and company jurisdiction. Do not rely on
preconfigured Pakistan statutory rates: no rates are seeded because rates and
rules vary by tax type, region, transaction, and effective date. Verify each
configuration against current official guidance and a qualified tax adviser.

## Reporting scope

The tax calculator supports configured sales, withholding, and income-tax rates.
It does not automatically post withholding or income-tax liabilities to invoices,
payments, payroll, or the general ledger. The transactions table and CSV export
cover posted sales-tax/VAT transactions only; they are for return preparation and
are not an electronic filing integration.
