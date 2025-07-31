#!/bin/bash
set -e

BUILD_PATH=$(ls /home/ubuntu/build/*.jar | grep -v 'plain' | head -n 1)
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

nohup java -jar "$JAR_NAME" \
  --spring.profiles.active=prod \
  --jwt.secret="${JWT_SECRET}" \
  --jwt.expiration-ms="${JWT_EXPIRATION_MS}" \
  --kakao.client-id="${KAKAO_CLIENT_ID}" \
  --kakao.auth-url="${KAKAO_AUTH_URL}" \
  --kakao.api-url="${KAKAO_API_URL}" \
  --kakao.redirect-uri="${KAKAO_REDIRECT_URI}" \
  --kakao.template-id="${KAKAO_TEMPLATE_ID}" \
  > "$APP_DIR/app.log" 2>&1 &

echo "▶ started! (bg)"
