# Plugin
## Execution
Run `mvn package` directly in the current project to run this plugin.

Add `opentelemetry-javaagent-all.jar` with `StaticInstrumenter` class and patch to your project main folder. 
This version of the project support newer releases of *OpenTelemetry* javaagent. 
You can find version used by use in the main folder (`opentelemetry-static-javaagent-all.jar`)

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
			<agentPath>opentelemetry-javaagent-static-all.jar</agentPath>
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
