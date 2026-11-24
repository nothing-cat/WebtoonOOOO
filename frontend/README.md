# Moodtoon frontend

원본 Webfront-moodtoon의 디자인, 컴포넌트, CSS, React Router를 재사용한 웹툰 커뮤니티입니다.

## 실행
```sh
cd frontend
npm ci
npm run dev
```

기본은 더미 모드입니다. demo/admin 아이디와 임의 테스트 비밀번호로 로그인할 수 있습니다. 실제 비밀번호는 입력하지 마세요. .env.local에서 VITE_USE_MOCK=false, VITE_API_BASE_URL=/api 설정 시 Spring Boot API를 호출합니다. 실제 API 및 세션/CSRF/CORS는 아직 구현 및 검증되지 않았습니다. 원본의 Supabase 직접 연결 및 KMAS 클라이언트는 이식하지 않았습니다. 원본 package 및 lock은 호환성을 위해 유지합니다(Supabase SDK는 미사용).
