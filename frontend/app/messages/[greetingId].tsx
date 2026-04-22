import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  KeyboardAvoidingView,
  Platform,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useAuth } from '../../src/features/auth';
import { listMessages, sendMessage, getGreeting } from '../../src/features/social';
import type { GreetingResponse, MessageResponse } from '../../src/features/social';
import { colors, spacing, borderRadius, touchTarget } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

const MAX_BODY_LENGTH = 140;
const TIMER_DURATION_MS = 30 * 60 * 1000; // 30분

function formatCountdown(ms: number): string {
  if (ms <= 0) return '00:00';
  const totalSec = Math.floor(ms / 1000);
  const min = Math.floor(totalSec / 60);
  const sec = totalSec % 60;
  return `${String(min).padStart(2, '0')}:${String(sec).padStart(2, '0')}`;
}

export default function MessagesScreen() {
  const { greetingId } = useLocalSearchParams<{ greetingId: string }>();
  const { user } = useAuth();
  const router = useRouter();

  const [messages, setMessages] = useState<MessageResponse[]>([]);
  const [greeting, setGreeting] = useState<GreetingResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [body, setBody] = useState('');
  const [sending, setSending] = useState(false);
  const [timeLeft, setTimeLeft] = useState<number>(TIMER_DURATION_MS);
  const flatListRef = useRef<FlatList>(null);

  const id = Number(greetingId);

  const load = useCallback(async () => {
    try {
      const [msgs, greet] = await Promise.all([listMessages(id), getGreeting(id)]);
      setMessages(msgs);
      setGreeting(greet);
      // 남은 시간 계산
      if (greet.expiresAt) {
        setTimeLeft(Math.max(0, greet.expiresAt - Date.now()));
      }
    } catch {
      // 에러 무시 — 화면에 빈 상태 표시
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  // 타이머 카운트다운
  useEffect(() => {
    if (timeLeft <= 0) return;
    const interval = setInterval(() => {
      setTimeLeft((prev) => {
        const next = prev - 1000;
        return next <= 0 ? 0 : next;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [timeLeft]);

  async function handleSend() {
    const trimmed = body.trim();
    if (!trimmed || sending) return;
    setSending(true);
    try {
      const msg = await sendMessage(id, trimmed);
      setMessages((prev) => [...prev, msg]);
      setBody('');
      flatListRef.current?.scrollToEnd({ animated: true });
    } catch {
      // 실패 시 메시지 유지 (사용자가 재시도 가능)
    } finally {
      setSending(false);
    }
  }

  const isExpired = greeting?.status === 'EXPIRED' || timeLeft <= 0;

  if (loading) {
    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} accessibilityLabel="불러오는 중" />
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container} edges={['top', 'bottom']}>
      {/* 헤더 */}
      <View style={styles.header}>
        <TouchableOpacity
          onPress={() => router.back()}
          style={styles.backButton}
          accessibilityLabel="뒤로 가기"
          accessibilityRole="button"
        >
          <Text style={styles.backText}>‹</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>메시지</Text>
        <View style={styles.timerContainer}>
          <Text style={[styles.timer, timeLeft < 5 * 60 * 1000 && styles.timerWarning]}>
            {isExpired ? '만료됨' : formatCountdown(timeLeft)}
          </Text>
        </View>
      </View>

      {/* 메시지 목록 */}
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        keyboardVerticalOffset={0}
      >
        <FlatList
          ref={flatListRef}
          data={messages}
          keyExtractor={(item) => String(item.id)}
          contentContainerStyle={styles.messageList}
          renderItem={({ item }) => {
            const isMine = item.senderId === user?.id;
            return (
              <View style={[styles.bubble, isMine ? styles.bubbleMine : styles.bubbleTheirs]}>
                <Text
                  style={[
                    styles.bubbleText,
                    isMine ? styles.bubbleTextMine : styles.bubbleTextTheirs,
                  ]}
                >
                  {item.body}
                </Text>
              </View>
            );
          }}
          ListEmptyComponent={
            <View style={styles.empty}>
              <Text style={styles.emptyText}>첫 메시지를 보내보세요</Text>
            </View>
          }
          onContentSizeChange={() => flatListRef.current?.scrollToEnd({ animated: false })}
        />

        {/* 입력란 */}
        {!isExpired && (
          <View style={styles.inputRow}>
            <TextInput
              style={styles.input}
              value={body}
              onChangeText={(text) => setBody(text.slice(0, MAX_BODY_LENGTH))}
              placeholder="메시지 입력..."
              placeholderTextColor={colors.textMuted}
              multiline
              accessibilityLabel="메시지 입력"
              returnKeyType="send"
            />
            <Text style={styles.charCount}>
              {body.length}/{MAX_BODY_LENGTH}
            </Text>
            <TouchableOpacity
              style={[styles.sendButton, (!body.trim() || sending) && styles.sendButtonDisabled]}
              onPress={handleSend}
              disabled={!body.trim() || sending}
              accessibilityLabel="메시지 전송"
              accessibilityRole="button"
            >
              <Text style={styles.sendText}>{sending ? '...' : '전송'}</Text>
            </TouchableOpacity>
          </View>
        )}

        {isExpired && (
          <View style={styles.expiredBanner}>
            <Text style={styles.expiredText}>대화 시간이 만료되었어요</Text>
          </View>
        )}
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
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    backgroundColor: colors.surface,
  },
  backButton: {
    minWidth: touchTarget.min,
    minHeight: touchTarget.min,
    alignItems: 'center',
    justifyContent: 'center',
  },
  backText: {
    fontSize: 28,
    color: colors.primary,
    lineHeight: 32,
  },
  headerTitle: {
    ...typography.subheading,
    color: colors.text,
    flex: 1,
    textAlign: 'center',
  },
  timerContainer: {
    minWidth: touchTarget.min,
    alignItems: 'flex-end',
    justifyContent: 'center',
  },
  timer: {
    ...typography.caption,
    color: colors.textMuted,
  },
  timerWarning: {
    color: colors.warning,
  },
  messageList: {
    padding: spacing.md,
    gap: spacing.sm,
  },
  bubble: {
    maxWidth: '75%',
    borderRadius: borderRadius.card,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  bubbleMine: {
    alignSelf: 'flex-end',
    backgroundColor: colors.primary,
  },
  bubbleTheirs: {
    alignSelf: 'flex-start',
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border,
  },
  bubbleText: {
    ...typography.body,
  },
  bubbleTextMine: {
    color: colors.surface,
  },
  bubbleTextTheirs: {
    color: colors.text,
  },
  empty: {
    alignItems: 'center',
    paddingTop: spacing.xl,
  },
  emptyText: {
    ...typography.body,
    color: colors.textMuted,
  },
  inputRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    padding: spacing.md,
    borderTopWidth: 1,
    borderTopColor: colors.border,
    backgroundColor: colors.surface,
    gap: spacing.sm,
  },
  input: {
    flex: 1,
    ...typography.body,
    color: colors.text,
    backgroundColor: colors.background,
    borderRadius: borderRadius.button,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    maxHeight: 100,
    minHeight: touchTarget.min,
  },
  charCount: {
    ...typography.micro,
    color: colors.textMuted,
    alignSelf: 'flex-end',
    paddingBottom: spacing.sm,
  },
  sendButton: {
    backgroundColor: colors.primary,
    borderRadius: borderRadius.button,
    paddingHorizontal: spacing.md,
    minHeight: touchTarget.min,
    alignItems: 'center',
    justifyContent: 'center',
  },
  sendButtonDisabled: {
    opacity: 0.4,
  },
  sendText: {
    ...typography.body,
    color: colors.surface,
    fontWeight: '600',
  },
  expiredBanner: {
    padding: spacing.md,
    backgroundColor: colors.border,
    alignItems: 'center',
  },
  expiredText: {
    ...typography.body,
    color: colors.textMuted,
  },
});
