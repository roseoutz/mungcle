import React, { Component } from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { colors, spacing } from '../../constants/theme';
import { typography } from '../../constants/typography';

interface Props {
  children: React.ReactNode;
  onRetry?: () => void;
}

interface State {
  hasError: boolean;
  isNetworkError: boolean;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, isNetworkError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    // 네트워크 에러 판별
    const isNetworkError =
      error.message.includes('Network') ||
      error.message.includes('fetch') ||
      error.message.includes('연결');
    return { hasError: true, isNetworkError };
  }

  handleRetry = () => {
    this.setState({ hasError: false, isNetworkError: false });
    this.props.onRetry?.();
  };

  render() {
    if (!this.state.hasError) {
      return this.props.children;
    }

    return (
      <View style={styles.container}>
        <Text style={styles.icon}>{this.state.isNetworkError ? '📡' : '⚠️'}</Text>
        <Text style={styles.title}>
          {this.state.isNetworkError ? '연결할 수 없어요' : '오류가 발생했어요'}
        </Text>
        <Text style={styles.message}>
          {this.state.isNetworkError
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
