import { useRouter } from 'expo-router';
import React, { useState } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, StyleSheet, Text } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../../src/features/auth';
import { Button } from '../../src/shared/components/Button';
import { TextField } from '../../src/shared/components/TextField';
import { colors, spacing } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

export default function EmailLoginScreen() {
  const router = useRouter();
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleLogin() {
    if (!email || !password) {
      setError('이메일과 비밀번호를 입력해주세요.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      await login(email, password);
      router.replace('/(tabs)');
    } catch {
      setError('이메일 또는 비밀번호가 올바르지 않습니다.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      >
        <ScrollView
          contentContainerStyle={styles.content}
          keyboardShouldPersistTaps="handled"
        >
          <Text style={styles.title}>이메일 로그인</Text>

          <TextField
            label="이메일"
            value={email}
            onChangeText={setEmail}
            placeholder="example@email.com"
            keyboardType="email-address"
            autoCapitalize="none"
            autoComplete="email"
          />

          <TextField
            label="비밀번호"
            value={password}
            onChangeText={setPassword}
            placeholder="비밀번호 입력"
            secureTextEntry
            autoComplete="password"
          />

          {error ? <Text style={styles.error}>{error}</Text> : null}

          <Button
            variant="primary"
            size="lg"
            loading={loading}
            onPress={handleLogin}
            accessibilityLabel="로그인"
          >
            로그인
          </Button>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  flex: {
    flex: 1,
  },
  content: {
    padding: spacing.lg,
    gap: spacing.sm,
  },
  title: {
    ...typography.heading,
    color: colors.text,
    marginBottom: spacing.md,
  },
  error: {
    ...typography.caption,
    color: colors.error,
    marginBottom: spacing.sm,
  },
});
