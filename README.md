# Plugin
## Execution
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
