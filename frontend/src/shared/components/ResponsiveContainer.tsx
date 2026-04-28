import React from 'react';
import { StyleSheet, View, type ViewStyle } from 'react-native';
import { useResponsive } from '../hooks/useResponsive';

interface Props {
  children: React.ReactNode;
  maxWidth?: number;
  style?: ViewStyle;
}

export function ResponsiveContainer({ children, maxWidth = 960, style }: Props) {
  const { isWeb } = useResponsive();

  if (!isWeb) {
    return <View style={[styles.base, style]}>{children}</View>;
  }

  return (
    <View style={[styles.base, styles.webContainer, { maxWidth }, style]}>
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  base: {
    flex: 1,
  },
  webContainer: {
    width: '100%' as unknown as number,
    alignSelf: 'center',
  },
});
