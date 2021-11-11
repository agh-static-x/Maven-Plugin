# Plugin

## Parameters

- `artifactName` - name of the artifact that should be instrumented. Plugin will search for
  artifact with this exact name. If such artifact won't be found, plugin will default to
  instrumenting all project artifacts.


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
