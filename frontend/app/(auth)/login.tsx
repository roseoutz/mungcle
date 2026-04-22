import { useRouter } from 'expo-router';
import React from 'react';
import { Alert, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '../../src/shared/components/Button';
import type { SocialProvider } from '../../src/features/auth/types/auth.types';
import { colors, spacing } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

const providerNames: Record<SocialProvider, string> = {
  KAKAO: '카카오',
  NAVER: '네이버',
  APPLE: 'Apple',
  GOOGLE: 'Google',
};

export default function LoginScreen() {
  const router = useRouter();

  function handleSocialLogin(provider: SocialProvider) {
    Alert.alert(
      '준비 중',
      `${providerNames[provider]} 로그인은 현재 준비 중입니다.\n이메일로 먼저 시작해보세요!`,
      [{ text: '확인' }]
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.hero}>
        <Text style={styles.logo}>멍클</Text>
        <Text style={styles.tagline}>반려견과 함께하는{'\n'}신뢰 기반 산책 커뮤니티</Text>
      </View>

      <View style={styles.actions}>
        {/* 이메일 로그인 (기본 경험) */}
        <Button
          variant="primary"
          size="lg"
          accessibilityLabel="이메일로 로그인"
          onPress={() => router.push('/(auth)/email-login')}
        >
          이메일로 로그인
        </Button>

        <Button
          variant="text"
          size="md"
          accessibilityLabel="이메일로 회원가입"
          onPress={() => router.push('/(auth)/register')}
        >
          이메일로 회원가입
        </Button>

        {/* 구분선 */}
        <View style={styles.dividerRow}>
          <View style={styles.dividerLine} />
          <Text style={styles.dividerText}>또는 소셜 계정으로</Text>
          <View style={styles.dividerLine} />
        </View>

        {/* 카카오 로그인 (활성, 준비 중 안내) */}
        <Button
          variant="outline"
          size="lg"
          accessibilityLabel="카카오로 시작하기"
          onPress={() => handleSocialLogin('KAKAO')}
        >
          카카오로 시작하기
        </Button>

        {/* 네이버 (출시 예정, 비활성) */}
        <View>
          <Button
            variant="outline"
            size="lg"
            accessibilityLabel="네이버로 시작하기 (출시 예정)"
            disabled
            onPress={() => handleSocialLogin('NAVER')}
          >
            네이버로 시작하기
          </Button>
          <Text style={styles.comingSoonLabel}>출시 예정</Text>
        </View>

        {/* 애플 (출시 예정, 비활성) */}
        <View>
          <Button
            variant="outline"
            size="lg"
            accessibilityLabel="Apple로 시작하기 (출시 예정)"
            disabled
            onPress={() => handleSocialLogin('APPLE')}
          >
            Apple로 시작하기
          </Button>
          <Text style={styles.comingSoonLabel}>출시 예정</Text>
        </View>

        {/* 구글 (출시 예정, 비활성) */}
        <View>
          <Button
            variant="outline"
            size="lg"
            accessibilityLabel="Google로 시작하기 (출시 예정)"
            disabled
            onPress={() => handleSocialLogin('GOOGLE')}
          >
            Google로 시작하기
          </Button>
          <Text style={styles.comingSoonLabel}>출시 예정</Text>
        </View>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
    paddingHorizontal: spacing.lg,
  },
  hero: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: spacing.md,
  },
  logo: {
    ...typography.display,
    color: colors.primary,
  },
  tagline: {
    ...typography.body,
    color: colors.textMuted,
    textAlign: 'center',
  },
  actions: {
    paddingBottom: spacing.xl,
    gap: spacing.sm,
  },
  dividerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    marginVertical: spacing.xs,
  },
  dividerLine: {
    flex: 1,
    height: 1,
    backgroundColor: colors.border,
  },
  dividerText: {
    ...typography.caption,
    color: colors.textMuted,
  },
  comingSoonLabel: {
    ...typography.caption,
    color: colors.textMuted,
    textAlign: 'center',
    marginTop: spacing.xs,
  },
});
