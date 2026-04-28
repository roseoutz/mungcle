import { usePathname, useRouter } from 'expo-router';
import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { colors, spacing } from '../../constants/theme';
import { typography } from '../../constants/typography';

interface NavItem {
  icon: string;
  label: string;
  path: string;
}

const NAV_ITEMS: NavItem[] = [
  { icon: '🏠', label: '홈', path: '/(tabs)' },
  { icon: '🐾', label: '내 강아지', path: '/(tabs)/dogs' },
  { icon: '🔔', label: '알림', path: '/(tabs)/notifications' },
  { icon: '⚙️', label: '설정', path: '/(tabs)/settings' },
];

export function WebSidebar() {
  const pathname = usePathname();
  const router = useRouter();

  const isActive = (path: string) => {
    if (path === '/(tabs)') return pathname === '/' || pathname === '/(tabs)';
    return pathname.startsWith(path.replace('/(tabs)', ''));
  };

  return (
    <View style={styles.container}>
      <Text style={styles.logo}>멍클</Text>

      <View style={styles.nav}>
        {NAV_ITEMS.map((item) => {
          const active = isActive(item.path);
          return (
            <Pressable
              key={item.path}
              style={[styles.navItem, active && styles.navItemActive]}
              onPress={() => router.push(item.path as Parameters<typeof router.push>[0])}
              accessibilityLabel={item.label}
              accessibilityRole="link"
            >
              <Text style={styles.navIcon}>{item.icon}</Text>
              <Text style={[styles.navLabel, active && styles.navLabelActive]}>
                {item.label}
              </Text>
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: 240,
    backgroundColor: colors.surface,
    borderRightWidth: 1,
    borderRightColor: colors.border,
    paddingTop: spacing.xl,
    paddingHorizontal: spacing.md,
  },
  logo: {
    ...typography.display,
    color: colors.primary,
    paddingHorizontal: spacing.sm,
    marginBottom: spacing.xl,
  },
  nav: {
    gap: spacing.xs,
  },
  navItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    paddingVertical: spacing.sm + 4,
    paddingHorizontal: spacing.md,
    borderRadius: 8,
  },
  navItemActive: {
    backgroundColor: `${colors.primary}14`,
  },
  navIcon: {
    fontSize: 20,
  },
  navLabel: {
    ...typography.body,
    color: colors.textMuted,
  },
  navLabelActive: {
    color: colors.primary,
    fontWeight: '600',
  },
});
