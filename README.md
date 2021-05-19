# Plugin
## Execution
Add opentelemetry-javaagent-all.jar with StaticInstrumenter class and patch to your project main folder.

Run `mvn package` in Maven Plugin project and install it to your local repository with comment:

```
mvn install:install-file -Dfile=./target/counter-maven-plugin-1.0-SNAPSHOT.jar -DgroupId=com.example -DartifactId=counter-maven-plugin -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
```

Add the following code to the `pom.xml` file of the project where you want to execute the plugin:
```
<build>
    <plugins>
        <plugin>
		<groupId>com.example</groupId>
		<artifactId>counter-maven-plugin</artifactId>
		<version>1.0-SNAPSHOT</version>
		<configuration>
			<currentBaseDir>${basedir}</currentBaseDir>
			<suffix>.java</suffix>
		</configuration>
		<executions>
			<execution>
				<goals>
					<goal>dependency</goal>
				</goals>
			</execution>
		</executions>
	</plugin>
    </plugins>
</build>
```

Run `mvn package` directly in the current project to run this plugin.
