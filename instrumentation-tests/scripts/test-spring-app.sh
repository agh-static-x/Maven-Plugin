#!/bin/bash

mvn --projects instrumentation-tests/test-apps/spring-test-app,instrumentation-tests/utils/fake-backend package

java -jar instrumentation-tests/utils/fake-backend/target/fake-backend-1.0-SNAPSHOT-shaded.jar &
BACKEND_PID=$!

export OTEL_TRACES_EXPORTER=otlp
export OTEL_EXPORTER_OTLP_ENDPOINT=http://127.0.0.1:8888/

java -jar instrumentation-tests/test-apps/spring-test-app/target/spring-test-app-0.0.1-SNAPSHOT-instrumented.jar &
SPRING_PID=$!

sleep 10;
curl -m 2 http://localhost:8080/greeting
sleep 10;
TRACES=$(curl -m 2 http://localhost:8888/get-traces)

kill -9 $SPRING_PID
kill -9 $BACKEND_PID

if [ "$TRACES" != "[]" ]; then
	echo "success, there are some traces"
	exit 0
else
	echo "failure, there are no traces"
	exit 1
fi