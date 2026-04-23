import React, { Component } from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { colors, spacing } from '../../constants/theme';
import { typography } from '../../constants/typography';

interface Props {
  children: React.ReactNode;
  onRetry?: () => void;
  // 커스텀 폴백 UI — ReactNode 또는 렌더 함수(error, retry) 모두 허용
  fallback?: React.ReactNode | ((error: Error, retry: () => void) => React.ReactNode);
}

interface State {
  hasError: boolean;
  error: Error | null;
  isNetworkError: boolean;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null, isNetworkError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    // 네트워크 에러 판별
    const isNetworkError =
      error.message.includes('Network') ||
      error.message.includes('fetch') ||
      error.message.includes('연결');
    return { hasError: true, error, isNetworkError };
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    // 렌더 에러 로깅 — 프로덕션에서는 Sentry 등으로 대체
    console.error('[ErrorBoundary] render error:', error.message, info.componentStack);
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: null, isNetworkError: false });
    this.props.onRetry?.();
  };

  render() {
    const { hasError, error, isNetworkError } = this.state;
    const { children, fallback } = this.props;

    if (!hasError) {
      return children;
    }

    // 커스텀 폴백 처리 — 렌더 함수 또는 ReactNode 모두 지원
    if (fallback !== undefined) {
      if (typeof fallback === 'function') {
        return (fallback as (error: Error, retry: () => void) => React.ReactNode)(
          error ?? new Error('Unknown error'),
          this.handleRetry,
        );
      }
      return fallback;
    }

    return (
      <View style={styles.container}>
        <Text style={styles.icon}>{isNetworkError ? '📡' : '⚠️'}</Text>
        <Text style={styles.title}>
          {isNetworkError ? '연결할 수 없어요' : '오류가 발생했어요'}
        </Text>
        <Text style={styles.message}>
          {isNetworkError
            ? '인터넷 연결을 확인하고 다시 시도해주세요'
            : '잠시 후 다시 시도해주세요'}
        </Text>
        <TouchableOpacity
          style={styles.retryButton}
          onPress={this.handleRetry}
          accessibilityLabel="다시 시도"
          accessibilityRole="button"
        >
          <Text style={styles.retryText}>다시 시도</Text>
        </TouchableOpacity>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: spacing.lg,
    backgroundColor: colors.background,
  },
  icon: {
    fontSize: 48,
    marginBottom: spacing.md,
  },
  title: {
    ...typography.subheading,
    color: colors.text,
    textAlign: 'center',
    marginBottom: spacing.sm,
  },
  message: {
    ...typography.body,
    color: colors.textMuted,
    textAlign: 'center',
    marginBottom: spacing.lg,
  },
  retryButton: {
    backgroundColor: colors.primary,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
    borderRadius: 8,
    minHeight: 44,
    alignItems: 'center',
    justifyContent: 'center',
  },
  retryText: {
    ...typography.body,
    color: colors.surface,
    fontWeight: '600',
  },
});
