import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../../src/features/auth';
import { Button } from '../../src/shared/components/Button';
import { colors, spacing } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

export default function SettingsScreen() {
  const { user, logout } = useAuth();

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.content}>
        <Text style={styles.title}>설정</Text>
        {user && (
          <Text style={styles.nickname}>{user.nickname}</Text>
        )}
        <View style={styles.spacer} />
        <Button
          variant="outline"
          size="md"
          onPress={logout}
          accessibilityLabel="로그아웃"
        >
          로그아웃
        </Button>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    flex: 1,
    padding: spacing.lg,
  },
  title: {
    ...typography.heading,
    color: colors.text,
    marginBottom: spacing.lg,
  },
  nickname: {
    ...typography.body,
    color: colors.textMuted,
  },
  spacer: {
    flex: 1,
  },
});
