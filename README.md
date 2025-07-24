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
rest-api.clientId=eb0390277e09b0ef2defdaaeea2c88a8

# 4. 카카오 OAuth2 인증 요청을 보낼 엔드포인트
#    - 사용자에게 로그인/동의 화면을 보여주는 URL
kakao.auth-url=https://kauth.kakao.com

# 5. 카카오 API 호출을 보낼 기본 URL
#    - 액세스 토큰 발급, 사용자 정보 조회 등 실제 API 요청에 사용
kakao.api-url=https://kapi.kakao.com

# 6. OAuth2 Redirect URI
#    - 카카오 로그인 후 authorization code 를 받을 콜백 URL
#    - 반드시 Kakao Developers 콘솔에도 동일하게 등록되어 있어야 함
kakao.redirect-uri=http://localhost:8080
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
| `rest-api.clientId`          | REST API 키                  | `abcd1234abcd1234abcd1234abcd1234`         |
| `kakao.redirect-uri`       | 카카오 인가 코드 콜백 URI     | `http://localhost:8080/` |
| `JWT_SECRET`               | 서비스 JWT 서명 키           | `ZQ06uDt8FTkJrY3G2u/qAc4boHirPmQPLmRiAetJgwk=` |

---

### 🎯 기능 요구사항 체크리스트

- [x] 카카오 Developers 앱 생성 & 로그인 활성화

- [x] Redirect URI (`http://localhost:8080/`) 등록

- [x] 환경 변수로 민감 정보 분리 (KAKAO_CLIENT_ID, JWT_SECRET 등)

- [x] WebClient로 인가 코드→토큰 교환 구현

- [x] WebClient로 토큰→유저 정보 조회 구현

- [x] JWT 발급: TokenService 재활용

- [x] Kakao API 에러 처리

- [x] 단위 테스트 구현

---

### 🗺️ HTTP API

1. 프론트엔드(Thymeleaf) 엔드포인트

|  메서드  | 경로       | 설명                              | 반환                            |
| :---: | :------- | :------------------------------ | :---------------------------- |
| `GET` | `/login` | 로그인 페이지 렌더링<br/>– 카카오 로그인 버튼 포함 | `templates/auth/login.html` 뷰 |

2. OAuth 콜백 처리 엔드포인트

|  메서드  | 경로  | 쿼리 파라미터           | 설명                                       | 동작                                             |
| :---: | :-- | :---------------- | :--------------------------------------- | :--------------------------------------------- |
| `GET` | `/` | `code` (optional) | 카카오로부터 인가 코드를 전달받는 콜백<br/>(Redirect URI) | 1. `code` 있으면 로그인<br/>2. 없으면 `/login` 으로 리다이렉트 |

3. 내부 서비스 간 HTTP 호출
    - 토큰 교환 (Authorization Code → Access Token)
        - 메서드: `POST {kakao.auth-url}/oauth/token`
        - Content-Type: `application/x-www-form-urlencoded `
        - 폼 필드:
           ```ini
           grant_type=authorization_code
           client_id={REST_API_KEY}
           redirect_uri={CALLBACK_URL}
           code={인가 코드}
           ```
        - 응답 바인딩: `KakaoTokenResponseDto`
           ```json
           {
              "access_token":"AAA",
              "refresh_token":"RRR",
              "expires_in":3600,
              "token_type":"Bearer"
           }
           ```
    - 사용자 정보 조회
        - 메서드: `GET {kakao.api-url}/v2/user/me`
        - 헤더:
           ```ini
           Authorization: Bearer {access_token}
           ```
        - 응답 바인딩: `KakaoUserResponseDto`
           ```json
           {
             "id": 999,
             "kakao_account": {
               "profile": { "nickname": "Neo" }
             }
           }
            ```

---

### 인가 URL 예시

```text
https://kauth.kakao.com/oauth/authorize
  ?client_id=${rest-api.clientId}
  &redirect_uri=${kakao.redirect-uri}    # http://localhost:8080
  &response_type=code
  &scope=profile_nickname
```