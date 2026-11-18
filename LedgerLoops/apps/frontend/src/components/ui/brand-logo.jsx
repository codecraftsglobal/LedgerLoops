import { useTheme } from 'next-themes';
import lightLogo from 'assets/images/brand/ledgerloop-logo.svg';
import darkLogo from 'assets/images/brand/ledgerloop-logo-dark.svg';

export function BrandLogo({ alt = 'LedgerLoop logo', ...props }) {
  const { resolvedTheme, theme } = useTheme();
  const activeTheme = resolvedTheme || theme || 'dark';

  return <img src={activeTheme === 'dark' ? darkLogo : lightLogo} alt={alt} {...props} />;
}
