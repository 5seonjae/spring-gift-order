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
kakao.clientId=

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

# 7. 메세지 템플릿 아이디
kakao.template-id=122845
```

Jwt Secret 키는 임의의 Base64 로 인코딩된 문자열을 `JWT_SECRET=` 값에 넣어주면 됩니다.

``kakao.client-id`` 에는 **앱의 REST API 키** 를 입력합니다.  
이 값은 개인‧프로덕션 자격 증명이므로 **절대로 Git 레포지토리에 커밋하거나 공개 저장소에 노출하지 마세요.**

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

## 📦 Step2 - 주문하기

### 🧩 기능 명세

1. 주문 작성

    - `POST /api/orders`
    - 요청 Body
      ```json
      {
        "optionId": 17,
        "quantity": 2,
        "message": "맛있게 부탁해요!"
      }
      ```
    - 동작
        1. 옵션 & 재고 검증 → 차감
        2. 주문 생성
        3. ( 주문자 ) 위시리스트에서 옵션 제거
        4. Kakao Message API 로 나에게 보내기 전송
    - 응답 예시 `201 Created`
      ```json
      {
        "id": 42,
        "optionId": 17,
        "quantity": 2,
        "orderDateTime": "2025‑07‑25T14:00:00",
        "message": "맛있게 부탁해요!"
      }
      ```

2. 주문 상세 조회 `GET /api/orders/{id}`
3. 주문 내역 페이징 `GET /api/orders?page=&size=`
4. 기타 보조 API ( Product, Option, Wish ) 는 `/api/**` 네임스페이스 유지

### 🔐 인증·인가 흐름

```Plane Text
sequenceDiagram
    actor User
    participant Front as Front‑end
    participant Server as Spring Boot
    participant Kakao as Kakao Server

    User->>Front: Kakao Login 버튼 클릭
    Front->>Kakao: /oauth/authorize?client_id&redirect_uri
    Kakao-->>Front: code=abc123 (302)
    Front->>Server: GET /login/oauth2/code/kakao?code=abc123
    Server->>Kakao: /oauth/token (code 교환)
    Kakao-->>Server: access_token
    Server->>Server: 회원 조회/가입 → JWT 생성
    Server-->>User: Set‑Cookie: JWT; HttpOnly
```

### 🗒️ 기능 구현 체크리스트

- [x] 도메인 모델 설계 ( Order )
- [x] **주문 생성 API** `POST /api/orders`
    - [x] 옵션 & 재고 검증
    - [x] 재고 차감 `option.decreaseStock()`
    - [x] 주문 엔티티 저장
    - [x] 주문자 위시리스트 항목 삭제
    - [x] Kakao *나에게 보내기* 메시지 전송
- [x] **주문 상세 조회 API** `GET /api/orders/{id}`
- [x] **주문 내역 페이징 API** `GET /api/orders`
- [x] 예외 처리 (404 Not Found, 409 Conflict, 502 Bad Gateway 등)
- [x] 단위 테스트 (카카오 API Stub)

---

## 🚀 Step 3 배포하기

지금까지 만든 선물하기 서비스를 **GitHub Actions → EC2** 파이프라인으로 자동 배포하고 클라이언트가 `https://gift.example.com`에서 안전하게 API를 호출하도록 구성합니다.

### 기능 목록

- CI/CD
  - [x] GitHub Actions 워크플로(`deploy.yml`) 작성
  - [x] JAR → EC2 업로드(`scp-action`) + 원격 재시작(`deploy.sh`)
- 보안 
  - [x] 글로벌 CORS 설정 (`/api/**`)
  - [ ] HTTPS(TLS) 적용 – Nginx + Let’s Encrypt

### 시스템 개요

```Plain text
┌────────────────────────┐   ┌────────────────────┐
│ GitHub Repo │ ─ push    ─▶ │   GitHub Actions   │
└────────────────────────┘   ├────────────────────┤
                             │   build → deploy   │
                             └─────────────────┬──┘
                                     ssh + scp │ 
┌────────────────────────┐           ┌─────────▼───────────┐
│       EC2 (Ubuntu)     │           │    deploy.sh runs   │
│ Nginx (TLS termination)│◀──https──▶│   Spring Boot JAR   │
└────────────────────────┘           └─────────────────────┘
```

## 요구 사항 & 사전 준비

| 항목 | 설명                                              |
|------|-------------------------------------------------|
|EC2| Ubuntu 22.04, OpenJDK 21, 포트 `8080` 오픈          |
|도메인| `gift.example.com` A-레코드 → EC2 IP               |
|GitHub Secrets| `EC2_HOST`, `EC2_SSH_KEY`, `EC2_USER` (=ubuntu) |
|AWS(OIDC)| `ROLE_TO_ASSUME` (선택) – Secrets 없이 배포하려면 사용     | 
|빌드 툴| Gradle Wrapper 포함 (`./gradlew`)                 |

## 디렉토리/파일

```Plane text
├── .github/workflows/deploy.yml # CI/CD 파이프라인
├── scripts/
│ └── deploy.sh # 서버 재시작 스크립트
└── src/ ... # 기존 백엔드 코드
```

## 5. GitHub Actions 워크플로 예시 (`deploy.yml`)

> 주요 Action
> * 파일 업로드 : **appleboy/scp-action** 
> * 원격 명령 : **appleboy/ssh-action** (동일 리포)
> * AWS OIDC  : **aws-actions/configure-aws-credentials**

```yaml
name: Deploy Gift Service
on:
  push:
    branches: [main, develop]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: ~/.gradle/caches
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}

      - name: Build JAR
        run: ./gradlew clean build

      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-jar
          path: build/libs/*.jar

  deploy:
    needs: build
    runs-on: ubuntu-latest
    concurrency:
      group: "deploy-${{ github.ref }}"
      cancel-in-progress: true           # 동일 브랜치 중복 배포 차단

    steps:
      - uses: actions/download-artifact@v4
        with:
          name: app-jar

      - name: Copy to EC2
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          source: "*.jar"
          target: "/home/ubuntu/build/"

      - name: Restart remote service
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            chmod +x ~/scripts/deploy.sh
            ~/scripts/deploy.sh
```

### 서버 재시작 스크립트 예시 (`scripts/deploy.sh`)

```bash
#!/bin/bash
set -e

BUILD_PATH=$(ls /home/ubuntu/build/*.jar | head -n 1)
JAR_NAME=$(basename "$BUILD_PATH")
APP_DIR=/home/ubuntu/app

echo "▶ current JAR  : $JAR_NAME"
PID=$(pgrep -f "$JAR_NAME" || true)

if [ -n "$PID" ]; then
echo "▶ stop running app (pid=$PID)"
kill -15 "$PID"
sleep 5
fi

echo "▶ deploy new JAR"
cp "$BUILD_PATH" "$APP_DIR/"
cd "$APP_DIR"

nohup java -jar "$JAR_NAME" --spring.profiles.active=prod \
> /dev/null 2>&1 &
echo "▶ started! (bg)"
```

### 글로벌 CORS 설정

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://gift.example.com")
                .allowedMethods("GET","POST","PUT","DELETE")
                .allowCredentials(true)
                .maxAge(1800);
    }
}
```

### HTTPS (Let’s Encrypt + Nginx) — 선택

```bash
# ① Nginx 설치
sudo apt-get update && sudo apt-get install nginx

# ② certbot
sudo snap install core; sudo snap refresh core
sudo snap install --classic certbot
sudo certbot --nginx -d gift.example.com
```

---