# 🇯🇵 OsakaFam 안드로이드 앱 — 설치 가이드

> 비개발자도 따라할 수 있는 단계별 가이드입니다.

---

## 프로젝트 파일 구조

```
OsakaFam/
├── build.gradle.kts              ← 프로젝트 설정
├── settings.gradle.kts
├── gradle.properties
├── app/
│   ├── build.gradle.kts          ← ★ API 키 설정 파일 ★
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/values/
│       └── java/com/osakafam/app/
│           ├── MainActivity.kt         ← 앱 진입점 + 하단 4탭
│           ├── PhraseData.kt           ← 회화/관광지/역 데이터
│           ├── network/
│           │   └── ApiService.kt       ← OpenAI·날씨·환율 API
│           └── ui/
│               ├── theme/Theme.kt      ← 웹앱과 동일한 컬러
│               └── screens/
│                   ├── HomeScreen.kt       ← 홈(날씨·환율·면세·가이드)
│                   ├── MapScreen.kt        ← 길찾기(관광지·노선도·IC카드)
│                   ├── TranslateScreen.kt  ← 번역(카메라·갤러리·AI)
│                   └── LanguageScreen.kt   ← 회화(필터·검색·TTS·커스텀)
```

---

## Step 1: Android Studio 설치

1. https://developer.android.com/studio 에서 다운로드
2. 설치 중 모든 기본 옵션 그대로 (Next만 누르기)
3. 첫 실행 시 SDK 자동 다운로드 (10~20분)

---

## Step 2: 프로젝트 만들기

1. Android Studio → **File → New → New Project**
2. **Empty Activity** 선택 → Next
3. 설정값:
   - Name: `OsakaFam`
   - Package name: `com.osakafam.app`
   - Language: `Kotlin`
   - Minimum SDK: `API 26`
4. Finish 클릭

---

## Step 3: 파일 교체

이 ZIP의 내용물을 프로젝트 폴더에 **전부 덮어쓰기** 하세요:

- `build.gradle.kts` (루트)
- `settings.gradle.kts`
- `gradle.properties`
- `app/build.gradle.kts`
- `app/src/main/` 폴더 전체

교체 후 Android Studio 상단의 **🐘 Sync Now** 버튼 클릭!

---

## Step 4: OpenAI API 키 설정 ⚠️ 중요!

`app/build.gradle.kts` 파일에서:

```
buildConfigField("String", "OPENAI_API_KEY", "\"여기에_본인_API키_입력\"")
```

↓ 실제 키로 교체:

```
buildConfigField("String", "OPENAI_API_KEY", "\"sk-proj-abc123...\"")
```

저장 후 **Sync Now** 클릭.

---

## Step 5: 실행

### 방법 A — 실제 폰 (추천)
1. 폰 설정 → 개발자 옵션 → USB 디버깅 ON
   - 개발자 옵션이 안 보이면: 설정 → 휴대전화 정보 → 빌드번호 7번 터치
2. USB로 연결 → Android Studio에서 ▶️ Run

### 방법 B — 에뮬레이터
1. Device Manager → Create Device → Pixel 6 선택
2. 시스템 이미지 다운로드 (API 34) → ▶️ Run

---

## Step 6: APK 만들기 (다른 폰에 설치)

1. 메뉴: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. 완료 후 `app-debug.apk` 파일을 카톡/메일로 전송
3. 받은 폰에서 설치 (출처 알 수 없는 앱 허용 필요)

---

## 웹앱과 안드로이드 앱 비교

| 구분 | 웹앱 | 안드로이드 앱 |
|------|------|-------------|
| 실행 방법 | 브라우저 접속 | 홈화면 아이콘 터치 |
| 날씨 | open-meteo API | ← 동일 |
| 환율 | open.er-api.com | ← 동일 (실시간) |
| 사진 번역 | Netlify → OpenAI | 폰에서 직접 OpenAI 호출 |
| 음성 재생 | Web Speech API | Android TTS (더 안정적) |
| 카메라 | 브라우저 카메라 | 네이티브 (더 빠름) |
| 회화 검색 | 텍스트 필터 | ← 동일 + 카테고리 칩 |
| 길찾기 | 구글맵 웹 | 구글맵 앱 자동 실행 |
| IC카드 계산 | 웹 | ← 동일 |
| 오프라인 | 불가 | 환율·회화 일부 가능 |

---

## 자주 발생하는 문제

**"Sync failed"** → File → Invalidate Caches → Restart

**앱 크래시** → Logcat에서 빨간 에러 확인 (대부분 API 키 문제)

**카메라 안 열림** → 설정 → 앱 → OsakaFam → 권한 → 카메라 허용

**"번역 실패"** → API 키 확인 + OpenAI 크레딧 잔액 확인

---

막히는 부분이 있으면 에러 메시지와 함께 다시 질문해주세요!
