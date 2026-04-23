import React, { useEffect, useRef } from 'react';
import { Animated, StyleSheet, View, type ViewStyle } from 'react-native';
import { colors, borderRadius, spacing } from '../../constants/theme';

type SkeletonShape = 'line' | 'circle' | 'card';

interface SkeletonLoaderProps {
  shape?: SkeletonShape;
  width?: number | `${number}%`;
  height?: number;
  style?: ViewStyle;
}

// 콘텐츠 로딩 중 애니메이션 플레이스홀더 — React Native Animated API 사용 (외부 의존성 없음)
export function SkeletonLoader({ shape = 'line', width, height, style }: SkeletonLoaderProps) {
  const opacity = useRef(new Animated.Value(0.4)).current;

  useEffect(() => {
    // 명도 0.4 ↔ 1.0 반복 페이드 애니메이션
    const animation = Animated.loop(
      Animated.sequence([
        Animated.timing(opacity, {
          toValue: 1,
          duration: 800,
          useNativeDriver: true,
        }),
        Animated.timing(opacity, {
          toValue: 0.4,
          duration: 800,
          useNativeDriver: true,
        }),
      ]),
    );
    animation.start();
    return () => animation.stop();
  }, [opacity]);

  const shapeStyle = getShapeStyle(shape, width, height);

  return (
    <Animated.View
      style={[styles.base, shapeStyle, style, { opacity }]}
      accessibilityElementsHidden
      importantForAccessibility="no-hide-descendants"
    />
  );
}

function getShapeStyle(
  shape: SkeletonShape,
  width?: number | `${number}%`,
  height?: number,
): ViewStyle {
  switch (shape) {
    case 'circle': {
      const size = height ?? 48;
      return {
        width: width ?? size,
        height: size,
        borderRadius: size / 2,
      };
    }
    case 'card':
      return {
        width: width ?? '100%',
        height: height ?? 120,
        borderRadius: borderRadius.card,
      };
    case 'line':
    default:
      return {
        width: width ?? '100%',
        height: height ?? 14,
        borderRadius: 6,
      };
  }
}

// 카드형 스켈레톤 — 아바타 + 텍스트 라인 조합
export function SkeletonCard() {
  return (
    <View style={skeletonCardStyles.container}>
      <SkeletonLoader shape="circle" height={56} />
      <View style={skeletonCardStyles.lines}>
        <SkeletonLoader shape="line" width="60%" height={14} />
        <SkeletonLoader shape="line" width="40%" height={12} style={{ marginTop: spacing.sm }} />
        <SkeletonLoader shape="line" width="80%" height={12} style={{ marginTop: spacing.sm }} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  base: {
    backgroundColor: colors.border,
  },
});

const skeletonCardStyles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    gap: spacing.md,
    padding: spacing.md,
    backgroundColor: colors.surface,
    borderRadius: borderRadius.card,
    marginBottom: spacing.sm,
  },
  lines: {
    flex: 1,
    justifyContent: 'center',
  },
});
