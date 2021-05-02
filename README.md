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
                <!-- basedir: maven system variables -->
                <currentBaseDir>${basedir}</currentBaseDir>
                <suffix>.java</suffix>
            </configuration>
            <!-- Must be mounted -->
            <executions>
                <execution>
                    <!-- execute this phrase in clean -->
                    <phase>clean</phase>
                    <goals>
                        <goal>dependency-counter</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Run `mvn clean` directly in the current project to run this plugin.
