import { useRouter } from 'expo-router';
import * as Location from 'expo-location';
import React, { useState } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '../../src/shared/components/Button';
import { TextField } from '../../src/shared/components/TextField';
import { colors, spacing } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

// GPS 좌표를 200m 그리드 셀 ID로 변환 (직접 저장 금지)
function toGridCell(lat: number, lng: number): string {
  const gridSize = 0.0018; // 약 200m
  const gridLat = Math.floor(lat / gridSize);
  const gridLng = Math.floor(lng / gridSize);
  return `${gridLat}_${gridLng}`;
}

export default function SetLocationScreen() {
  const router = useRouter();
  const [neighborhood, setNeighborhood] = useState('');
  const [loading, setLoading] = useState(false);
  const [gpsLoading, setGpsLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleGpsDetect() {
    setGpsLoading(true);
    setError('');
    try {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== 'granted') {
        setError('위치 권한이 필요합니다. 직접 동네를 입력해주세요.');
        return;
      }
      const loc = await Location.getCurrentPositionAsync({});
      // GPS 좌표 직접 저장 금지 — 그리드 셀만 사용
      const gridCell = toGridCell(loc.coords.latitude, loc.coords.longitude);
      setNeighborhood(gridCell);
    } catch {
      setError('위치를 가져올 수 없습니다. 직접 입력해주세요.');
    } finally {
      setGpsLoading(false);
    }
  }

  async function handleSubmit() {
    if (!neighborhood) {
      setError('동네를 입력하거나 GPS로 설정해주세요.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      // TODO: 서버에 neighborhood 저장 API 연동
      router.replace('/(tabs)');
    } catch {
      setError('동네 설정에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.title}>동네 설정</Text>
        <Text style={styles.subtitle}>산책할 동네를 설정해주세요</Text>

        <Button
          variant="outline"
          size="md"
          onPress={handleGpsDetect}
          loading={gpsLoading}
          accessibilityLabel="GPS로 현재 위치 자동 설정"
        >
          GPS로 자동 설정
        </Button>

        <Text style={styles.divider}>또는 직접 입력</Text>

        <TextField
          label="동네"
          value={neighborhood}
          onChangeText={setNeighborhood}
          placeholder="예: 서울 마포구 합정동"
          error={error || undefined}
        />

        {gpsLoading && (
          <ActivityIndicator color={colors.primary} accessibilityLabel="위치 감지 중" />
        )}

        <Button
          variant="primary"
          size="lg"
          loading={loading}
          onPress={handleSubmit}
          accessibilityLabel="동네 설정 완료"
        >
          완료
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
    gap: spacing.md,
  },
  title: {
    ...typography.heading,
    color: colors.text,
  },
  subtitle: {
    ...typography.body,
    color: colors.textMuted,
  },
  divider: {
    ...typography.caption,
    color: colors.textMuted,
    textAlign: 'center',
  },
});
