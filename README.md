# Plugin
## Execution
Run `mvn package` directly in the current project to run this plugin.

!!! Try it out with [this](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.1.0/opentelemetry-javaagent-all.jar) distribution of OTEL. !!!

Add `opentelemetry-javaagent-all.jar` to your project folder. You can get recent release on https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases.

Run `mvn package` in *Maven Plugin* project and install it to your local repository with comment:

```
mvn install:install-file -Dfile=./target/dependency-inspector-maven-plugin-1.0-SNAPSHOT.jar -DgroupId=com.example -DartifactId=dependency-inspector-maven-plugin -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
```

Add the following code to the `pom.xml` file of the project where you want to execute the plugin:
```
<build>
	<plugins>
		<plugin>
		<groupId>com.example</groupId>
		<artifactId>dependency-inspector-maven-plugin</artifactId>
		<version>1.0-SNAPSHOT</version>
		<configuration>
			<agentPath>{path_to_opentelemetry-javaagent-all.jar}</agentPath>
		</configuration>
		<executions>
			<execution>
				<goals>
					<goal>dependency-inspection</goal>
				</goals>
			</execution>
		</executions>
	</plugin>
	</plugins>
</build>
```

Run `mvn package` directly in the current project to run this plugin.
