# Plugin
## Execution
Run `mvn package` directly in the current project to run this plugin.

!!! Try it out with [this](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.1.0/opentelemetry-javaagent-all.jar) distribution of OTEL. !!!

Add `opentelemetry-javaagent-all.jar` to your project folder. You can get recent release on https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases.

Run `mvn package` in *Maven Plugin* project and install it to your local repository with comment:

```
mvn --projects app-instrumenter install:install-file -Dfile=./target/opentelemetry-java-instrumenter-maven-plugin-1.0-SNAPSHOT.jar -DgroupId=agh.edu.pl -DartifactId=opentelemetry-java-instrumenter-maven-plugin -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
```

Add the following code to the `pom.xml` file of the project where you want to execute the plugin:
```
<build>
	<plugins>
		<plugin>
		<groupId>agh.edu.pl</groupId>
		<artifactId>opentelemetry-java-instrumenter-maven-plugin</artifactId>
		<version>1.0-SNAPSHOT</version>
		<configuration>
			<agentPath>{path_to_opentelemetry-javaagent-all.jar}</agentPath>

			<exporter>
				<otlpExporter>
					<tracesExporter>otlp</tracesExporter>
					<metricExporter>otlp</metricExporter>

					<exporterOtlpEndpoint></exporterOtlpEndpoint>
					<exporterOtlpTracesEndpoint></exporterOtlpTracesEndpoint>
					<exporterOtlpMetricsEndpoint></exporterOtlpMetricsEndpoint>

					<exporterOtlpCertificate></exporterOtlpCertificate>
					<exporterOtlpTracesCertificate></exporterOtlpTracesCertificate>
					<exporterOtlpMetricsCertificate></exporterOtlpMetricsCertificate>

					<exporterOtlpHeaders></exporterOtlpHeaders>
					<exporterOtlpTracesHeaders></exporterOtlpTracesHeaders>
					<exporterOtlpMetricsHeaders></exporterOtlpMetricsHeaders>

					<exporterOtlpCompression></exporterOtlpCompression>
					<exporterOtlpTracesCompression></exporterOtlpTracesCompression>
					<exporterOtlpMetricsCompression></exporterOtlpMetricsCompression>

					<exporterOtlpTimeout></exporterOtlpTimeout>
					<exporterOtlpTracesTimeout></exporterOtlpTracesTimeout>
					<exporterOtlpMetricsTimeout></exporterOtlpMetricsTimeout>

					<exporterOtlpProtocol>grpc</exporterOtlpProtocol>
					<exporterOtlpTracesProtocol>grpc</exporterOtlpTracesProtocol>
					<exporterOtlpMetricsProtocol>grpc</exporterOtlpMetricsProtocol>
				</otlpExporter>
				<jaegerExporter>
					<tracesExporter>jaeger</tracesExporter>

					<exporterJaegerEndpoint></exporterJaegerEndpoint>
					<exporterJaegerTimeout></exporterJaegerTimeout>
				</jaegerExporter>
				<zipkinExporter>
					<tracesExporter>zipkin</tracesExporter>

					<exporterZipkinEndpoint></exporterZipkinEndpoint>
				</zipkinExporter>
				<prometheusExporter>
					<metricExporter>prometheus</metricExporter>

					<prometheusPort></prometheusPort>
					<prometheusHost></prometheusHost>
				</prometheusExporter>
				<loggingExporter>
					<tracesExporter>logging</tracesExporter>
					<metricExporter>logging</metricExporter>

					<exporterLoggingPrefix></exporterLoggingPrefix>
				</loggingExporter>
			</exporter>

			<propagators>
				<propagator>tracecontext</propagator>
				<propagator>baggage</propagator>
			</propagators>

			<openTelemetryResource>
					<attributes>
						<key1>value1</key1>
						<key2>value2</key2>
					</attributes>
					<serviceName></serviceName>
					<javaDisabledResourceProviders>
						<provider>path.to.resource.provider1</provider>
						<provider>path.to.resource.provider2</provider>
					</javaDisabledResourceProviders>
			</openTelemetryResource>

			<batchSpanProcessor>
				<bspScheduleDelay>5000</bspScheduleDelay>
				<bspMaxQueueSize>2048</bspMaxQueueSize>
				<bspMaxExportBatchSize>512</bspMaxExportBatchSize>
				<bspExportTimeout>30000</bspExportTimeout>
			</batchSpanProcessor>

			<sampler>
				<alwaysOn></alwaysOn>
				<alwaysOff></alwaysOff>
				<traceIdRatio></traceIdRatio>
				<parentBasedAlwaysOn></parentBasedAlwaysOn>
				<parentBasedAlwaysOff></parentBasedAlwaysOff>
				<parentBasedTraceIdRatio></parentBasedTraceIdRatio>
			</sampler>

			<spanLimits>
				<spanAttributeValueLengthLimit></spanAttributeValueLengthLimit>
				<spanAttributeCountLimit>128</spanAttributeCountLimit>
				<spanEventCountLimit>128</spanEventCountLimit>
				<spanLinkCountLimit>128</spanLinkCountLimit>
			</spanLimits>

			<metricsExemplarFilter>WITH_SAMPLED_TRACE</metricsExemplarFilter>

			<imrExportInterval>60000</imrExportInterval>
		</configuration>
		<executions>
			<execution>
				<goals>
					<goal>instrument-with-opentelemetry</goal>
				</goals>
			</execution>
		</executions>
	</plugin>
	</plugins>
</build>
```

Run `mvn package` directly in the current project to run this plugin.

## Spotless
To apply Spotless run `mvn --projects agent-instrumenter,app-instrumenter spotless:apply`.
