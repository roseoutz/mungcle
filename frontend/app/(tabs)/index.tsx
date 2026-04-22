import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../../src/features/auth';
import { colors, spacing } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

export default function HomeScreen() {
  const { user } = useAuth();

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.content}>
        <Text style={styles.greeting}>
          안녕하세요, {user?.nickname ?? '멍클 사용자'}님 👋
        </Text>
        <Text style={styles.sub}>근처 반려견과 산책을 시작해보세요.</Text>
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
    justifyContent: 'center',
    alignItems: 'center',
  },
  greeting: {
    ...typography.heading,
    color: colors.text,
    marginBottom: spacing.sm,
    textAlign: 'center',
  },
  sub: {
    ...typography.body,
    color: colors.textMuted,
    textAlign: 'center',
  },
});
