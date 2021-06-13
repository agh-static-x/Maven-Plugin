package com.example.agent_instrumentation;

import com.example.agent_instrumentation.advices.*;
import io.opentelemetry.javaagent.BytesAndName;
import io.opentelemetry.javaagent.PostTransformer;
import io.opentelemetry.javaagent.PreTransformer;
import io.opentelemetry.javaagent.StaticInstrumenter;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class OpenTelemetryLoader {
    public URLClassLoader otelClassLoader;
    public Class<?> openTelemetryAgentClass;
    public String otelJarPath;

    public final String OTEL_AGENT_NAME = "io.opentelemetry.javaagent.OpenTelemetryAgent";
    String TMP_DIR = "./tmpOtel";

    public OpenTelemetryLoader(String otelJarPath) {
        this.otelJarPath = otelJarPath;
    }

    public void instrument() throws IOException {
        loadOtel(new File(otelJarPath));
        File tmpDir = new File(TMP_DIR);
        tmpDir.mkdir();
        File copyFile = new File(TMP_DIR+"/"+otelJarPath);
        Files.copy(new File(otelJarPath).toPath(), copyFile.toPath());
        instrumentOpenTelemetryAgent(copyFile);
        injectClasses(copyFile);
    }

    public synchronized void loadOtel(File otelJar){
        try {
            otelClassLoader = new URLClassLoader(
                new URL[] {otelJar.toURI().toURL()},
                OpenTelemetryLoader.class.getClassLoader()
            );

            openTelemetryAgentClass = Class.forName(OTEL_AGENT_NAME, true, otelClassLoader);
            System.out.println("Loaded OpenTelemetryAgent: " + openTelemetryAgentClass);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public void instrumentOpenTelemetryAgent(File jarFile) throws IOException {
        new ByteBuddy()
            .rebase(openTelemetryAgentClass)
            .visit(Advice.to(PrintingAdvices.class).on(isMethod()))
            .visit(Advice.to(OpenTelemetryAgentAdvices.class).on(
                isMethod()
                    .and(named("agentmain"))
                    .and(takesArguments(2))
                )
            )
            .make()
            .inject(jarFile);
//                .load(ClassLoader.getSystemClassLoader())
//                .getLoaded();
    }

    public void injectClasses(File jarFile) throws IOException {
        List<Class<?>> classesToInject = List.of(
            BytesAndName.class,
            PreTransformer.class,
            PostTransformer.class,
            StaticInstrumenter.class
        );

        for(var clazz : classesToInject) {
            String[] clazzNameParts = clazz.getName().split("\\.");
            String clazzName = clazzNameParts[clazzNameParts.length - 1];

            new ByteBuddy()
                .rebase(clazz)
                .name("io.opentelemetry.javaagent." + clazzName)
                .make()
                .inject(jarFile);
            System.out.println("Instrumented " + clazzName);
        }
    }
}
