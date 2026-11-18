import { useEffect, useMemo, useState } from 'react';
import { Download, FileText, Calculator } from 'lucide-react';
import { toast } from 'sonner';

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { DataTable } from '@/components/ui/data-table';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader } from 'components';
import { authApi } from 'utils';

const taxTypes = [
  { value: 'SALES_TAX', label: 'Sales tax / GST' },
  { value: 'WITHHOLDING', label: 'Withholding tax' },
  { value: 'INCOME_TAX', label: 'Income / advance tax' },
];

const currencyFormatter = new Intl.NumberFormat(undefined, {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

function TaxReport() {
  const [loading, setLoading] = useState(true);
  const [transactions, setTransactions] = useState([]);
  const [taxRates, setTaxRates] = useState([]);
  const [taxType, setTaxType] = useState('SALES_TAX');
  const [taxCode, setTaxCode] = useState('');
  const [taxDate, setTaxDate] = useState(new Date().toISOString().slice(0, 10));
  const [taxableAmount, setTaxableAmount] = useState('');
  const [calculation, setCalculation] = useState(null);
  const [calculating, setCalculating] = useState(false);

  useEffect(() => {
    let active = true;
    const requests = [
      authApi({ method: 'GET', url: '/rest/taxes/getTaxRates?taxType=ALL' }),
      authApi({
        method: 'GET',
        url: '/rest/taxes/getVatTransationList?pageNo=0&pageSize=10000&paginationDisable=true',
      }),
    ];

    Promise.all(requests)
      .then(([ratesResponse, transactionsResponse]) => {
        if (!active) return;
        setTaxRates(Array.isArray(ratesResponse.data) ? ratesResponse.data : []);
        const transactionData = transactionsResponse.data?.data;
        setTransactions(Array.isArray(transactionData) ? transactionData : []);
      })
      .catch(error => {
        if (!active) return;
        toast.error(error?.response?.data?.message || 'Unable to load tax information.');
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const availableRates = useMemo(
    () => taxRates.filter(rate => rate.taxType === taxType),
    [taxRates, taxType]
  );

  const taxCodeOptions = useMemo(() => {
    const codes = new Map();
    availableRates.forEach(rate => {
      if (!rate.taxCode) return;
      const current = codes.get(rate.taxCode);
      if (!current) {
        codes.set(rate.taxCode, rate.name || rate.taxCode);
      } else if (rate.name && !current.includes(rate.name)) {
        codes.set(rate.taxCode, `${current}, ${rate.name}`);
      }
    });
    return Array.from(codes, ([code, name]) => ({ code, name }));
  }, [availableRates]);

  useEffect(() => {
    if (!taxCodeOptions.some(option => option.code === taxCode)) {
      setTaxCode(taxCodeOptions[0]?.code || '');
    }
  }, [taxCodeOptions, taxCode]);

  const transactionColumns = useMemo(
    () => [
      {
        accessorKey: 'invoiceDate',
        header: 'Transaction date',
        cell: ({ row }) => row.original.invoiceDate || row.original.date || '-',
      },
      {
        accessorKey: 'customerName',
        header: 'Party',
        cell: ({ row }) => row.original.customerName || '-',
      },
      {
        accessorKey: 'invoiceNumber',
        header: 'Reference',
        cell: ({ row }) => row.original.invoiceNumber || '-',
      },
      {
        accessorKey: 'referenceType',
        header: 'Document type',
      },
      {
        accessorKey: 'vatType',
        header: 'Tax account',
      },
      {
        accessorKey: 'amount',
        header: 'Taxable amount',
        cell: ({ row }) => currencyFormatter.format(Number(row.original.amount) || 0),
      },
      {
        accessorKey: 'vatAmount',
        header: 'Tax amount',
        cell: ({ row }) => currencyFormatter.format(Number(row.original.vatAmount) || 0),
      },
    ],
    []
  );

  const reportTotals = useMemo(
    () =>
      transactions.reduce(
        (totals, transaction) => ({
          taxableAmount: totals.taxableAmount + (Number(transaction.amount) || 0),
          taxAmount: totals.taxAmount + (Number(transaction.vatAmount) || 0),
        }),
        { taxableAmount: 0, taxAmount: 0 }
      ),
    [transactions]
  );

  const calculateTax = async event => {
    event.preventDefault();
    if (!taxCode || !taxDate || taxableAmount === '' || Number(taxableAmount) < 0) {
      toast.error('Choose a tax code, date, and non-negative taxable amount.');
      return;
    }

    setCalculating(true);
    try {
      const response = await authApi({
        method: 'POST',
        url: '/rest/taxes/calculate',
        data: { taxType, taxCode, taxDate, taxableAmount },
      });
      setCalculation(response.data);
    } catch (error) {
      setCalculation(null);
      toast.error(error?.response?.data?.message || 'Tax calculation failed.');
    } finally {
      setCalculating(false);
    }
  };

  const exportTransactions = () => {
    const headers = [
      'Transaction date',
      'Party',
      'Reference',
      'Document type',
      'Tax account',
      'Taxable amount',
      'Tax amount',
      'Tax registration number',
    ];
    const rows = transactions.map(transaction => [
      transaction.invoiceDate || transaction.date,
      transaction.customerName,
      transaction.invoiceNumber,
      transaction.referenceType,
      transaction.vatType,
      transaction.amount,
      transaction.vatAmount,
      transaction.taxRegistrationNo,
    ]);
    const csvValue = value => `"${String(value ?? '').replace(/"/g, '""')}"`;
    const csv = [headers, ...rows].map(row => row.map(csvValue).join(',')).join('\r\n');
    const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'ledgerloop-tax-transactions.csv';
    link.click();
    URL.revokeObjectURL(url);
  };

  if (loading) {
    return <Loader />;
  }

  return (
    <div className="tax-report-screen space-y-6">
      <Card>
        <CardHeader>
          <div className="flex items-center gap-3">
            <Calculator className="h-6 w-6 text-primary" />
            <CardTitle className="text-xl">Tax calculator</CardTitle>
          </div>
          <CardDescription>
            Calculate sales, withholding, and income taxes using your configured effective-dated
            rates. Confirm rates with your tax adviser before filing.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={calculateTax} className="grid gap-4 md:grid-cols-2 lg:grid-cols-5">
            <div className="space-y-2">
              <Label htmlFor="taxType">Tax type</Label>
              <select
                id="taxType"
                className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"
                value={taxType}
                onChange={event => {
                  setTaxType(event.target.value);
                  setCalculation(null);
                }}
              >
                {taxTypes.map(type => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="taxCode">Tax code</Label>
              <select
                id="taxCode"
                className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"
                value={taxCode}
                onChange={event => {
                  setTaxCode(event.target.value);
                  setCalculation(null);
                }}
              >
                <option value="">Select a configured tax code</option>
                {taxCodeOptions.map(option => (
                  <option key={option.code} value={option.code}>
                    {option.code} — {option.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="taxDate">Tax date</Label>
              <Input
                id="taxDate"
                type="date"
                value={taxDate}
                onChange={event => {
                  setTaxDate(event.target.value);
                  setCalculation(null);
                }}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="taxableAmount">Taxable amount</Label>
              <Input
                id="taxableAmount"
                type="number"
                min="0"
                step="0.01"
                value={taxableAmount}
                onChange={event => {
                  setTaxableAmount(event.target.value);
                  setCalculation(null);
                }}
                placeholder="0.00"
              />
            </div>
            <div className="flex items-end">
              <Button type="submit" disabled={calculating || !taxCode} className="w-full">
                {calculating ? 'Calculating…' : 'Calculate tax'}
              </Button>
            </div>
          </form>
          {calculation && (
            <div className="mt-5 grid gap-3 rounded-lg border border-border bg-muted/40 p-4 sm:grid-cols-3">
              <div>
                <p className="text-sm text-muted-foreground">Taxable amount</p>
                <p className="font-semibold">{currencyFormatter.format(calculation.taxableAmount)}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Calculated tax</p>
                <p className="font-semibold">{currencyFormatter.format(calculation.taxAmount)}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Effective rate</p>
                <p className="font-semibold">{calculation.effectiveRate}%</p>
              </div>
            </div>
          )}
          {taxRates.length === 0 && (
            <p className="mt-4 text-sm text-muted-foreground">
              No tax rates are configured yet. Add rates under Tax Rates before calculating.
            </p>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-3">
              <FileText className="h-6 w-6 text-primary" />
              <CardTitle className="text-xl">Sales-tax transactions</CardTitle>
            </div>
            <CardDescription className="mt-1">
              Posted tax transactions with a CSV export for return preparation.
            </CardDescription>
          </div>
          <Button variant="outline" onClick={exportTransactions} disabled={!transactions.length}>
            <Download className="mr-2 h-4 w-4" />
            Export CSV
          </Button>
        </CardHeader>
        <CardContent>
          <div className="mb-4 grid gap-3 sm:grid-cols-2">
            <div className="rounded-lg border border-border p-3">
              <p className="text-sm text-muted-foreground">Taxable total</p>
              <p className="text-lg font-semibold">
                {currencyFormatter.format(reportTotals.taxableAmount)}
              </p>
            </div>
            <div className="rounded-lg border border-border p-3">
              <p className="text-sm text-muted-foreground">Tax total</p>
              <p className="text-lg font-semibold">
                {currencyFormatter.format(reportTotals.taxAmount)}
              </p>
            </div>
          </div>
          <DataTable columns={transactionColumns} data={transactions} />
        </CardContent>
      </Card>
    </div>
  );
}

export default TaxReport;
