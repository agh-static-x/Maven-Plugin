package com.example.config;

public enum Propagator {
    TraceContext,
    Baggage,
    B3Single,
    B3Multi,
    Jaeger,
    XRay,
    OTTrace
}
