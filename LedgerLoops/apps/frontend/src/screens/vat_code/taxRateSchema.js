import { z } from 'zod';

const percentagePattern = /^(100(\.00?)?|[1-9]?\d(\.\d\d?)?)$/;
const amountPattern = /^\d+(\.\d{1,2})?$/;

export const taxRateSchema = z
  .object({
    name: z
      .string()
      .min(1, 'Tax rate name is required')
      .max(30, 'Name is too long')
      .regex(/^[a-zA-Z0-9 ]+$/, 'Name must contain only letters, numbers, and spaces'),
    vat: z.string().min(1, 'Percentage is required').regex(percentagePattern, 'Invalid percentage'),
    taxType: z.enum(['SALES_TAX', 'WITHHOLDING', 'INCOME_TAX']),
    taxCode: z
      .string()
      .min(1, 'Tax code is required')
      .max(64, 'Tax code is too long')
      .regex(/^[A-Za-z0-9_-]+$/, 'Use letters, numbers, underscores, or hyphens'),
    countryIsoCode: z
      .string()
      .optional()
      .refine(value => !value || /^[A-Za-z]{3}$/.test(value), 'Enter a 3-letter ISO country code'),
    regionCode: z.string().max(64, 'Region is too long').optional(),
    effectiveFrom: z.string().optional(),
    effectiveTo: z.string().optional(),
    calculationMode: z.enum(['FLAT_RATE', 'PROGRESSIVE_BANDS']),
    minimumAmount: z.string().optional(),
    maximumAmount: z.string().optional(),
  })
  .superRefine((value, context) => {
    for (const field of ['minimumAmount', 'maximumAmount']) {
      if (value[field] && !amountPattern.test(value[field])) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Enter a non-negative amount with up to two decimals',
          path: [field],
        });
      }
    }

    if (value.effectiveFrom && value.effectiveTo && value.effectiveTo < value.effectiveFrom) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'End date must be on or after the start date',
        path: ['effectiveTo'],
      });
    }

    if (value.calculationMode === 'PROGRESSIVE_BANDS') {
      if (!value.minimumAmount || !amountPattern.test(value.minimumAmount)) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'A valid minimum amount is required for a tax band',
          path: ['minimumAmount'],
        });
      }
      if (value.maximumAmount && !amountPattern.test(value.maximumAmount)) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Enter a valid maximum amount',
          path: ['maximumAmount'],
        });
      }
      if (
        value.minimumAmount &&
        value.maximumAmount &&
        amountPattern.test(value.minimumAmount) &&
        amountPattern.test(value.maximumAmount) &&
        Number(value.maximumAmount) <= Number(value.minimumAmount)
      ) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Maximum amount must exceed minimum amount',
          path: ['maximumAmount'],
        });
      }
    }
  });

export const normalizeTaxRateForm = value => ({
  ...value,
  taxType: value.taxType || 'SALES_TAX',
  taxCode: value.taxCode.trim().toUpperCase(),
  countryIsoCode: value.countryIsoCode?.trim().toUpperCase() || null,
  regionCode: value.regionCode?.trim() || null,
  effectiveFrom: value.effectiveFrom || null,
  effectiveTo: value.effectiveTo || null,
  calculationMode: value.calculationMode || 'FLAT_RATE',
  minimumAmount: value.minimumAmount || null,
  maximumAmount: value.maximumAmount || null,
});
