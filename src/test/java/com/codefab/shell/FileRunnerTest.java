package com.codefab.shell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FileRunner 테스트")
class FileRunnerTest {

    private ByteArrayOutputStream outStream;
    private ByteArrayOutputStream errStream;
    private FileRunner runner;

    @BeforeEach
    void setUp() {
        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();
        runner = new FileRunner(
                new PrintStream(outStream, true, StandardCharsets.UTF_8),
                new PrintStream(errStream, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return outStream.toString(StandardCharsets.UTF_8).trim().replace("\r\n", "\n");
    }

    private String error() {
        return errStream.toString(StandardCharsets.UTF_8).trim().replace("\r\n", "\n");
    }

    // ── Cycle 1: 정상 파일 실행 ───────────────────────────────────────────

    @Test
    @DisplayName("[run] PASS - 존재하는 파일을 실행하면 출력이 나오고 exit code 0을 반환한다")
    void run_PASS_존재하는_파일을_실행하면_출력이_나온다(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path file = tempDir.resolve("test.cfab");
        Files.writeString(file, "print 42;");

        // Act
        int exitCode = runner.run(file.toString());

        // Assert
        assertEquals(0, exitCode);
        assertEquals("42", output());
    }
}
