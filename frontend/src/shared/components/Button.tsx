import React from 'react';
import {
  ActivityIndicator,
  StyleSheet,
  Text,
  TouchableOpacity,
  type TouchableOpacityProps,
} from 'react-native';
import { colors, borderRadius, touchTarget } from '../../constants/theme';
import { typography } from '../../constants/typography';

type Variant = 'primary' | 'outline' | 'text';
type Size = 'sm' | 'md' | 'lg';

interface ButtonProps extends Omit<TouchableOpacityProps, 'style'> {
  variant?: Variant;
  size?: Size;
  loading?: boolean;
  children: string;
  accessibilityLabel: string;
}

export function Button({
  variant = 'primary',
  size = 'md',
  loading = false,
  disabled = false,
  onPress,
  children,
  accessibilityLabel,
  ...rest
}: ButtonProps) {
  const isDisabled = disabled || loading;

  return (
    <TouchableOpacity
      style={[
        styles.base,
        styles[variant],
        styles[size],
        isDisabled && styles.disabled,
      ]}
      onPress={onPress}
      disabled={isDisabled}
      accessibilityLabel={accessibilityLabel}
      accessibilityRole="button"
      accessibilityState={{ disabled: isDisabled, busy: loading }}
      {...rest}
    >
      {loading ? (
        <ActivityIndicator
          color={variant === 'primary' ? colors.surface : colors.primary}
          size="small"
        />
      ) : (
        <Text style={[styles.label, styles[`${variant}Label`], styles[`${size}Label`]]}>
          {children}
        </Text>
      )}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  base: {
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: borderRadius.button,
    minHeight: touchTarget.min,
  },
  // variant styles
  primary: {
    backgroundColor: colors.primary,
  },
  outline: {
    backgroundColor: 'transparent',
    borderWidth: 1.5,
    borderColor: colors.primary,
  },
  text: {
    backgroundColor: 'transparent',
  },
  disabled: {
    opacity: 0.4,
  },
  // size styles
  sm: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    minHeight: touchTarget.min,
  },
  md: {
    paddingHorizontal: 20,
    paddingVertical: 12,
    minHeight: touchTarget.min,
  },
  lg: {
    paddingHorizontal: 28,
    paddingVertical: 16,
    minHeight: touchTarget.min,
  },
  // label base
  label: {
    fontWeight: '600',
  },
  // label variant
  primaryLabel: {
    color: colors.surface,
  },
  outlineLabel: {
    color: colors.primary,
  },
  textLabel: {
    color: colors.primary,
  },
  // label size
  smLabel: {
    fontSize: typography.caption.fontSize,
    lineHeight: typography.caption.lineHeight,
  },
  mdLabel: {
    fontSize: typography.body.fontSize,
    lineHeight: typography.body.lineHeight,
  },
  lgLabel: {
    fontSize: typography.subheading.fontSize,
    lineHeight: typography.subheading.lineHeight,
  },
});
