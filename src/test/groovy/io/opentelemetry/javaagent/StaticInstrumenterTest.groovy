package io.opentelemetry.javaagent

import spock.lang.Specification

class StaticInstrumenterTest extends Specification {

    def "test successful stream copying"() {
        setup:
        byte[] inputBytes = "Some bytes here".getBytes()
        InputStream inStream = new ByteArrayInputStream(inputBytes)
        OutputStream expectedOutStream = new ByteArrayOutputStream()
        expectedOutStream.write(inputBytes, 0, inputBytes.length)

        when:
        OutputStream actualOutStream = new ByteArrayOutputStream()
        StaticInstrumenter.copy(inStream, actualOutStream)

        then:
        actualOutStream.toByteArray() == inputBytes
    }

}
