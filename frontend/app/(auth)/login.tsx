import { useRouter } from 'expo-router';
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '../../src/shared/components/Button';
import { colors, spacing } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

export default function LoginScreen() {
  const router = useRouter();

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.hero}>
        <Text style={styles.logo}>멍클</Text>
        <Text style={styles.tagline}>반려견과 함께하는{'\n'}신뢰 기반 산책 커뮤니티</Text>
      </View>

      <View style={styles.actions}>
        {/* 카카오 로그인 — 추후 SDK 연동 */}
        <Button
          variant="primary"
          size="lg"
          accessibilityLabel="카카오로 시작하기"
          onPress={() => {
            // TODO: 카카오 OAuth 연동
          }}
        >
          카카오로 시작하기
        </Button>

        <Button
          variant="outline"
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
});
