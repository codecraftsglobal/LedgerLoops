/**
 * NeuBadge - Badge with Neumorphic Styling
 * Migration wrapper for reactstrap Badge
 */
import React from 'react';
import { cn } from '@/lib/utils';

const colorMap = {
  primary: {
    background: 'var(--neu-primary, #0f766e)',
    color: '#ffffff',
  },
  secondary: {
    background: 'var(--neu-text-muted, #98afc2)',
    color: '#ffffff',
  },
  success: {
    background: 'var(--neu-secondary, #168a75)',
    color: '#ffffff',
  },
  danger: {
    background: 'var(--neu-danger, #ff4d6a)',
    color: '#ffffff',
  },
  warning: {
    background: 'var(--neu-warning, #a16207)',
    color: '#ffffff',
  },
  info: {
    background: '#17a2b8',
    color: '#ffffff',
  },
  light: {
    background: '#f6f8f7',
    color: 'var(--neu-text-primary, #1e3a5f)',
  },
  dark: {
    background: 'var(--neu-text-primary, #1e3a5f)',
    color: '#ffffff',
  },
};

const NeuBadge = React.forwardRef(
  ({ children, className, color = 'primary', pill = false, style, ...props }, ref) => {
    const colorStyles = colorMap[color] || colorMap.primary;

    return (
      <span
        ref={ref}
        className={cn(
          'inline-flex items-center justify-center px-2 py-0.5 text-xs font-medium',
          pill ? 'rounded-full' : 'rounded',
          className
        )}
        style={{
          ...colorStyles,
          ...style,
        }}
        {...props}
      >
        {children}
      </span>
    );
  }
);
NeuBadge.displayName = 'NeuBadge';

export { NeuBadge };
