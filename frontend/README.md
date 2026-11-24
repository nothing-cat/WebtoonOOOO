# WebtoonOOOO 프론트엔드

기존 Vite/React 프로젝트를 기반으로 구현. 기존 Webfront-moodtoon의 WebtoonCard, WebtoonGrid, SearchBar 구조를 새 API DTO에 맞게 수정하여 재활용했다. 날씨/감정/Supabase Auth 클라이언트는 가져오지 않았다.

## 실행
```sh
cd frontend
npm ci
npm run dev
npm run build
```

기본은 더미 모드이며 모든 작품과 순위는 가상 데이터다. demo/admin 아이디와 임의의 테스트 비밀번호로 UI 권한 확인 가능. 가입 시 비밀번호는 저장하거나 검증하지 않는다. 실제 비밀번호를 입력하지 말 것. 브라우저 localStorage에 데모 활동만 저장된다. 초기화는 개발자 도구에서 webtoonoooo-demo-v1 키 삭제.

## 실제 API 연동
.env.example을 .env.local로 복사하고 VITE_USE_MOCK=false로 변경. VITE_API_BASE_URL=/api 이면 개발 서버에서 localhost:8080으로 프록시한다. 별도 배포 시 실제 백엔드 API URL을 설정한다.
현재 백엔드는 /api/db-test만 확인됨. 실제 서비스 API 및 인증은 아직 구현되지 않아 실연동 검증 전이다. API client는 쿠키 세션을 가정하며 서버 측 CORS, CSRF, 권한 검증을 추가해야 한다. 비밀키를 VITE_ 변수에 넣지 않는다.
응답은 JSON 객체 또는 배열(목록)이며 DTO는 src/mocks/data.js와 src/mocks/mockApi.js를 참고한다. 서버가 페이지네이션 객체를 사용하면 client를 수정해야 한다.
경로: /auth/signup, /auth/login, /auth/logout, /users/me 및 /favorites, /comments, /posts, /post-comments; /webtoons, /webtoons/popular, /webtoons/{id}, /favorite, /reviews, /comments; /genres, /platforms; /comments/{id}, /like; /posts, /posts/popular, /posts/{id}, /like, /comments; /post-comments/{id}. 모든 경로 앞에 /api.
라우팅은 추가 의존성 없이 해시 기반으로 구성했다. 페이지 직접 접근 시 정적 호스팅 rewrite 설정이 필요 없다.
