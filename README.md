# spring-gift-order

### .env 파일 준비

프로젝트 루트에 `.env` 파일이 존재해야 프로젝트가 정상 실행됩니다.

프로젝트 루트에 `.env` 파일을 생성하고, 아래 항목을 추가합니다.

```bash
# 1. JWT Secret 키 (Base64 인코딩된 문자열)
JWT_SECRET=

# 2. JWT 만료시간 (밀리초 단위)
JWT_EXPIRATION_MS=3600000

# 3. 카카오 로그인 REST API 키 (Client ID)
KAKAO_REST_API_CLIENT_ID=eb0390277e09b0ef2defdaaeea2c88a8
```

Jwt Secret 키는 임의의 Base64 로 인코딩된 문자열을 `JWT_SECRET=` 값에 넣어주면 됩니다.

---

## 이전 미션 `README.md`

### Spring-gift-product `README.md` - https://github.com/5seonjae/spring-gift-product/blob/step3/README.md
### Spring-gift-wishlist `README.md` - https://github.com/5seonjae/spring-gift-wishlist/blob/step3/README.md
### Spring-gift-enhancement `README.md` - https://github.com/5seonjae/spring-gift-enhancement/blob/step3/README.md

---

## 🚀 Step 1 – 카카오 로그인

### ⚙️ 환경 변수(.env 또는 IDE 설정)

| 환경 변수                  | 설명                         | 예시                                         |
|----------------------------|----------------------------|---------------------------------------------|
| `KAKAO_CLIENT_ID`          | REST API 키                  | `abcd1234abcd1234abcd1234abcd1234`         |
| `KAKAO_CLIENT_SECRET`      | (선택) Client Secret         | `secret-xyz0987654321`                     |
| `KAKAO_REDIRECT_URI`       | 카카오 인가 코드 콜백 URI     | `http://localhost:8080/api/members/login/kakao` |
| `JWT_SECRET`               | 서비스 JWT 서명 키           | `ZQ06uDt8FTkJrY3G2u/qAc4boHirPmQPLmRiAetJgwk=` |

---

### 🎯 기능 요구사항 체크리스트

- [ ] 카카오 Developers 앱 생성 & 로그인 활성화

- [ ] Redirect URI (/api/members/login/kakao) 등록

- [ ] 환경 변수로 민감 정보 분리 (KAKAO_CLIENT_ID, JWT_SECRET 등)

- [ ] WebClient로 인가 코드→토큰 교환 구현

- [ ] WebClient로 토큰→유저 정보 조회 구현

- [ ] JWT 발급: TokenService 재활용

- [ ] Kakao API 에러 처리

- [ ] 단위 테스트 / 통합 테스트 구현

---

### 🗺️ HTTP API

| 메서드 | 경로                                           | 설명                                                                                 |
|--------|-----------------------------------------------|--------------------------------------------------------------------------------------|
| GET    | `/login/kakao`                                 | 카카오 로그인 버튼 클릭 → 카카오 인가 URL로 리다이렉트                                    |
| GET    | `/`                                            | **Redirect URI** (`http://localhost:8080`) 콜백<br/>`code` 파라미터가 있으면 내부 API로 포워딩 |
| GET    | `/api/members/login/kakao?code={authorization}` | 인가 코드 수신 → WebClient로 토큰 교환 → 사용자 정보 조회 → JWT 생성 → JSON 응답               |

---

### 인가 URL 예시

```text
https://kauth.kakao.com/oauth/authorize
  ?client_id=${KAKAO_CLIENT_ID}
  &redirect_uri=${KAKAO_REDIRECT_URI}    # http://localhost:8080
  &response_type=code
  &scope=account_email,profile_nickname
```