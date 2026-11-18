import { Controller } from 'react-hook-form';
import { FormGroup, Input, Label } from 'components/migration';

const taxTypes = [
  { value: 'SALES_TAX', label: 'Sales tax / GST' },
  { value: 'WITHHOLDING', label: 'Withholding tax' },
  { value: 'INCOME_TAX', label: 'Income / advance tax' },
];

const calculationModes = [
  { value: 'FLAT_RATE', label: 'Flat percentage' },
  { value: 'PROGRESSIVE_BANDS', label: 'Progressive tax band' },
];

function TaxRateFields({ control, errors }) {
  return (
    <>
      <FormGroup>
        <Label htmlFor="taxType">Tax type</Label>
        <Controller
          name="taxType"
          control={control}
          render={({ field }) => (
            <select id="taxType" className="form-control" {...field}>
              {taxTypes.map(type => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
          )}
        />
      </FormGroup>
      <FormGroup>
        <Label htmlFor="taxCode">Tax code</Label>
        <Controller
          name="taxCode"
          control={control}
          render={({ field }) => (
            <Input
              id="taxCode"
              maxLength={64}
              placeholder="For example, PK_GST_GOODS"
              {...field}
              onChange={event => field.onChange(event.target.value.toUpperCase())}
              className={errors.taxCode ? 'is-invalid' : ''}
            />
          )}
        />
        {errors.taxCode && <div className="invalid-feedback d-block">{errors.taxCode.message}</div>}
      </FormGroup>
      <FormGroup>
        <Label htmlFor="countryIsoCode">Country ISO alpha-3 (optional)</Label>
        <Controller
          name="countryIsoCode"
          control={control}
          render={({ field }) => (
            <Input
              id="countryIsoCode"
              maxLength={3}
              placeholder="PAK"
              {...field}
              onChange={event => field.onChange(event.target.value.toUpperCase())}
              className={errors.countryIsoCode ? 'is-invalid' : ''}
            />
          )}
        />
        {errors.countryIsoCode && (
          <div className="invalid-feedback d-block">{errors.countryIsoCode.message}</div>
        )}
      </FormGroup>
      <FormGroup>
        <Label htmlFor="regionCode">Province / jurisdiction (optional)</Label>
        <Controller
          name="regionCode"
          control={control}
          render={({ field }) => (
            <Input
              id="regionCode"
              maxLength={64}
              placeholder="Leave blank for federal/national rates"
              {...field}
            />
          )}
        />
      </FormGroup>
      <FormGroup>
        <Label htmlFor="effectiveFrom">Effective from</Label>
        <Controller
          name="effectiveFrom"
          control={control}
          render={({ field }) => <Input id="effectiveFrom" type="date" {...field} />}
        />
      </FormGroup>
      <FormGroup>
        <Label htmlFor="effectiveTo">Effective to (optional)</Label>
        <Controller
          name="effectiveTo"
          control={control}
          render={({ field }) => (
            <Input
              id="effectiveTo"
              type="date"
              {...field}
              className={errors.effectiveTo ? 'is-invalid' : ''}
            />
          )}
        />
        {errors.effectiveTo && (
          <div className="invalid-feedback d-block">{errors.effectiveTo.message}</div>
        )}
      </FormGroup>
      <FormGroup>
        <Label htmlFor="calculationMode">Calculation method</Label>
        <Controller
          name="calculationMode"
          control={control}
          render={({ field }) => (
            <select id="calculationMode" className="form-control" {...field}>
              {calculationModes.map(mode => (
                <option key={mode.value} value={mode.value}>
                  {mode.label}
                </option>
              ))}
            </select>
          )}
        />
      </FormGroup>
      <FormGroup>
        <Label htmlFor="minimumAmount">Tax band minimum (progressive rates)</Label>
        <Controller
          name="minimumAmount"
          control={control}
          render={({ field }) => (
            <Input
              id="minimumAmount"
              type="number"
              min="0"
              step="0.01"
              {...field}
              className={errors.minimumAmount ? 'is-invalid' : ''}
            />
          )}
        />
        {errors.minimumAmount && (
          <div className="invalid-feedback d-block">{errors.minimumAmount.message}</div>
        )}
      </FormGroup>
      <FormGroup>
        <Label htmlFor="maximumAmount">Tax band maximum (blank for top band)</Label>
        <Controller
          name="maximumAmount"
          control={control}
          render={({ field }) => (
            <Input
              id="maximumAmount"
              type="number"
              min="0"
              step="0.01"
              {...field}
              className={errors.maximumAmount ? 'is-invalid' : ''}
            />
          )}
        />
        {errors.maximumAmount && (
          <div className="invalid-feedback d-block">{errors.maximumAmount.message}</div>
        )}
      </FormGroup>
    </>
  );
}

export default TaxRateFields;
