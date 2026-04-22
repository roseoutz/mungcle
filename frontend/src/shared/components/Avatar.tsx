import React from 'react';
import { Image, StyleSheet, Text, View } from 'react-native';
import { colors, borderRadius } from '../../constants/theme';
import { typography } from '../../constants/typography';

interface AvatarProps {
  uri?: string | null;
  size: number;
  fallback?: string;
  accessibilityLabel?: string;
}

export function Avatar({ uri, size, fallback, accessibilityLabel }: AvatarProps) {
  const containerStyle = {
    width: size,
    height: size,
    borderRadius: borderRadius.avatar,
  };

  if (uri) {
    return (
      <Image
        source={{ uri }}
        style={[styles.image, containerStyle]}
        accessibilityLabel={accessibilityLabel ?? '프로필 사진'}
      />
    );
  }

  return (
    <View
      style={[styles.fallbackContainer, containerStyle]}
      accessibilityLabel={accessibilityLabel ?? fallback ?? '프로필'}
    >
      <Text style={[styles.fallbackText, { fontSize: size * 0.4 }]}>
        {fallback?.charAt(0).toUpperCase() ?? '?'}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  image: {
    resizeMode: 'cover',
  },
  fallbackContainer: {
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  fallbackText: {
    color: colors.surface,
    fontWeight: typography.heading.fontWeight,
  },
});
