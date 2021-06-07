package com.example.agent_instrumentation;

import com.example.agent_instrumentation.advices.*;
import com.example.agent_instrumentation.instrumentation.*;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class OpenTelemetryLoader {
    public URLClassLoader otelClassLoader;
    public Class<?> openTelemetryAgentClass;
    public String otelJarPath;

    public final String OTEL_AGENT_NAME = "io.opentelemetry.javaagent.OpenTelemetryAgent";

    public OpenTelemetryLoader(String otelJarPath) {
        this.otelJarPath = otelJarPath;
    }

    public void instrument() throws IOException {
        loadOtel(new File(otelJarPath));
        instrumentOpenTelemetryAgent();
        injectClasses();
    }

    public synchronized void loadOtel(File otelJar){
        try {
            otelClassLoader = new URLClassLoader(
                new URL[] {otelJar.toURI().toURL()},
                OpenTelemetryLoader.class.getClassLoader()
            );

            openTelemetryAgentClass = Class.forName(OTEL_AGENT_NAME, true, otelClassLoader);
            System.out.println("Loaded OpenTelemetryAgent: " + openTelemetryAgentClass);
        } catch (MalformedURLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void instrumentOpenTelemetryAgent() throws IOException {
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
            .inject(new File(otelJarPath));
//                .load(ClassLoader.getSystemClassLoader())
//                .getLoaded();
    }

    public void injectClasses() throws IOException {
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
                .inject(new File(otelJarPath));
            System.out.println("Instrumented " + clazzName);
        }
    }
}
