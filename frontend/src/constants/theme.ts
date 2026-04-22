export const colors = {
  primary: '#4A7C59',       // forest green
  secondary: '#8B6914',     // warm brown
  background: '#FBF8F1',    // warm cream
  surface: '#FFFFFF',
  text: '#1A1A1A',
  textMuted: '#6B7280',
  safe: '#22C55E',          // 사회성 4-5
  caution: '#EAB308',       // 사회성 3
  warning: '#EF4444',       // 사회성 1-2
  border: '#E5E7EB',
  error: '#EF4444',
} as const;

export const spacing = {
  xs: 4, sm: 8, md: 16, lg: 24, xl: 32,
} as const;

export const borderRadius = {
  card: 12, button: 8, avatar: 50, chip: 16,
} as const;

export const touchTarget = { min: 44 } as const;
