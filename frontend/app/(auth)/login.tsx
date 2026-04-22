import { useRouter } from 'expo-router';
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '../../src/shared/components/Button';
import { useAuth } from '../../src/features/auth/hooks/useAuth';
import type { SocialProvider } from '../../src/features/auth/types/auth.types';
import { colors, spacing } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

export default function LoginScreen() {
  const router = useRouter();
  const { socialLogin } = useAuth();

  const handleSocialLogin = async (provider: SocialProvider) => {
    // TODO: 각 프로바이더 SDK에서 액세스 토큰을 획득한 후 socialLogin 호출
    // 예시: const token = await KakaoLogin.getAccessToken();
    // await socialLogin(provider, token);
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.hero}>
        <Text style={styles.logo}>멍클</Text>
        <Text style={styles.tagline}>반려견과 함께하는{'\n'}신뢰 기반 산책 커뮤니티</Text>
      </View>

      <View style={styles.actions}>
        {/* 카카오 로그인 */}
        <Button
          variant="primary"
          size="lg"
          accessibilityLabel="카카오로 시작하기"
          onPress={() => handleSocialLogin('KAKAO')}
        >
          카카오로 시작하기
        </Button>

        {/* 네이버 로그인 */}
        <Button
          variant="primary"
          size="lg"
          accessibilityLabel="네이버로 시작하기"
          onPress={() => handleSocialLogin('NAVER')}
        >
          네이버로 시작하기
        </Button>

        {/* 애플 로그인 */}
        <Button
          variant="primary"
          size="lg"
          accessibilityLabel="Apple로 시작하기"
          onPress={() => handleSocialLogin('APPLE')}
        >
          Apple로 시작하기
        </Button>

        {/* 구글 로그인 */}
        <Button
          variant="primary"
          size="lg"
          accessibilityLabel="Google로 시작하기"
          onPress={() => handleSocialLogin('GOOGLE')}
        >
          Google로 시작하기
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
