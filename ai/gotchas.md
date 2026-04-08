# 알려진 문제 / AI가 자주 틀리는 것

세션을 넘어서 누적되는 실패 기록. 같은 실수를 반복하지 않기 위한 목록.

## Prisma

- **스키마 변경 후 `npx prisma generate` 필수.** 안 하면 타입이 안 맞아서 빌드 에러.
- **마이그레이션 파일 수정 금지.** 이미 적용된 마이그레이션을 고치면 DB 상태가 깨짐. 새 마이그레이션을 만들 것.
- **`findUnique`에 `include` 쓸 때** 타입이 자동으로 확장되지 않을 수 있음. 반환 타입을 명시적으로 체크.

## Supabase

- **RLS(Row Level Security) 정책을 빼먹기 쉬움.** 새 테이블 만들면 반드시 RLS 설정. 안 하면 모든 데이터가 공개됨.
- **Storage 버킷 생성 시** public/private 설정 확인. 개 사진은 public, 예방접종 사진은 private.
- **서비스 키(service_role)는 백엔드에서만.** 프론트엔드 코드에 절대 노출 금지.

## Expo / React Native

- **expo-location 권한 요청** 순서가 중요. `requestForegroundPermissionsAsync()` 먼저, 거부되면 수동 동네 선택 fallback.
- **expo-notifications 설정** FCM 키가 `app.json`의 `android.googleServicesFile`에 있어야 함.
- **SafeAreaView** 빼먹으면 노치/홈바에 콘텐츠가 가려짐. 모든 최상위 화면에 적용.
- **`expo-router`에서 `(group)` 폴더** 이름에 괄호가 URL에 영향 안 줌. 하지만 `_layout.tsx` 빼먹으면 라우팅 깨짐.

## 200m 그리드

- **경계값 주의:** 적도(lat=0), 날짜변경선(lng=180/-180), 음수 좌표에서 grid snap이 다르게 동작할 수 있음. 반드시 테스트.
- **grid cell ID 형식:** `"37.564_126.978"` 같은 문자열로 저장. 숫자 연산이 아닌 문자열 비교로 검색.

## 인사하기 플로우

- **동시에 서로 인사 보내는 경우:** unique constraint로 중복 방지. 하지만 두 번째 요청이 409를 받으면 "이미 상대가 인사했어요"로 처리해야 함 (에러가 아님).
- **5분 만료 체크:** 서버 시간 기준. 클라이언트 시간과 차이 날 수 있음.

---

> 이 파일은 개발 중 발견되는 문제를 계속 추가한다. 문제를 발견하면 여기에 기록할 것.
