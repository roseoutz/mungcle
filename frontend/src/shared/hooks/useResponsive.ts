import { useWindowDimensions, Platform } from 'react-native';

export type Breakpoint = 'mobile' | 'tablet' | 'desktop';

const BREAKPOINTS = {
  tablet: 768,
  desktop: 1024,
} as const;

export function useResponsive() {
  const { width } = useWindowDimensions();

  const breakpoint: Breakpoint =
    width >= BREAKPOINTS.desktop
      ? 'desktop'
      : width >= BREAKPOINTS.tablet
        ? 'tablet'
        : 'mobile';

  return {
    breakpoint,
    isMobile: breakpoint === 'mobile',
    isTablet: breakpoint === 'tablet',
    isDesktop: breakpoint === 'desktop',
    isWeb: Platform.OS === 'web',
    showSidebar: Platform.OS === 'web' && width >= BREAKPOINTS.tablet,
    contentColumns: breakpoint === 'desktop' ? 3 : breakpoint === 'tablet' ? 2 : 1,
    width,
  };
}
