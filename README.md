# OpenTelemetry Java static instrumentation

## Project structure

Project consists of the following modules:

- **app-instrumenter** - Maven plugin, which uses instrumented OpenTelemetry agent to statically instrument
  specified project's artifacts. Responsible for repackaging and injecting needed agent code to final jar.
- **agent-instrumenter** - logic for instrumenting OpenTelemetry agent. Automatically injects instrumented
  agent to **app-instrumenter** module resources.
- **instrumentation-tests** - tests, which verify whether instrumentation is actually applied. Configured
  with Github Actions.

## Parameters

- `artifactName` - name of the artifact that should be instrumented. Plugin will search for artifact with this exact
  name. If such artifact won't be found, plugin will default to instrumenting all project artifacts.

- `outputFolder` - path to output folder for instrumented JAR. If no name for the folder is provided, the
  instrumented JAR will be stored in the default folder for Maven project.

- `suffix` - suffix will be added to your file name after the instrumentation. The default suffix is '-instrumented'
  (e.g 'example-2.1.jar' will become 'example-2.1-instrumented.jar').

- `noSuffix` - if set to true, no suffix will be added to file name.

## Execution

### Add plugin to app

1. Add following config to project's `pom.xml` file
```xml
<plugin>
    <groupId>agh.edu.pl</groupId>
    <artifactId>app-instrumenter</artifactId>
    <version>1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <goals>
                <goal>instrument</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

2. Run `mvn package` in project's folder.

3. Run instrumented app with `-Dio.opentelemetry.javaagent.shaded.io.opentelemetry.context.contextStorageProvider=default` system property.

Example: `java -jar -Dio.opentelemetry.javaagent.shaded.io.opentelemetry.context.contextStorageProvider=default target/your-app-instrumented.jar`
### Build plugin

Make sure that `opentelemetry-javaagent.jar` file is present in this repo root folder.

1. If any changes to **agent-instrumenter** are made, *AgentInstrumenter* class must be run. It takes OpenTelemetry agent
from resources, instruments it and injects to **app-instrumenter** resources.

**NOTE:** If this step is omitted, pre-commit hook will fail.

2. Run `mvn --projects app-instrumenter package`

3. Run `mvn --projects app-instrumenter install`

## Code quality
Project uses Spotless. To apply Spotless

* to all modules run `mvn spotless:apply`
* to a specific module run `mvn --projects <module-name> spotless:apply`
