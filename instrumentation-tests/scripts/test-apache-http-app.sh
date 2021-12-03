#!/bin/bash

mvn --projects instrumentation-tests/test-apps/apache-http-test-app package

java -jar instrumentation-tests/utils/fake-backend/target/fake-backend-1.0-SNAPSHOT-shaded.jar &
BACKEND_PID=$!

export OTEL_TRACES_EXPORTER=otlp
export OTEL_EXPORTER_OTLP_ENDPOINT=http://127.0.0.1:8888/


java -Dio.opentelemetry.javaagent.shaded.io.opentelemetry.context.contextStorageProvider=default -jar ./instrumentation-tests/test-apps/apache-http-test-app/target/apache-http-test-app-1.0-SNAPSHOT-shaded-instrumented.jar
APP_PID=$!

TRACES=$(curl -m 2 http://localhost:8888/get-traces)

kill -9 $APP_PID
kill -9 $BACKEND_PID

if [ "$TRACES" != "[]" ]; then
	echo "success, there are some traces"
	exit 0
else
	echo "failure, there are no traces"
	exit 1
fi