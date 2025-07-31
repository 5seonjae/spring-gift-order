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

nohup java -jar "$JAR_NAME" --spring.profiles.active=prod \
> "$APP_DIR/app.log" 2>&1 &
echo "▶ started! (bg)"
