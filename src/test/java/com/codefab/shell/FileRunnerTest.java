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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "print 42;");

        // Act
        int exitCode = runner.run(file.toString());

        // Assert
        assertEquals(0, exitCode);
        assertEquals("42", output());
    }

    // ── Cycle 2: 파일 없음 ───────────────────────────────────────────────

    @Test
    @DisplayName("[run] FAIL - 존재하지 않는 파일이면 에러 메시지를 출력하고 exit code 66을 반환한다")
    void run_FAIL_존재하지_않는_파일이면_에러를_반환한다() {
        // Act
        int exitCode = runner.run("존재하지않는파일.txt");

        // Assert
        assertEquals(66, exitCode);
        assertEquals("[오류] 파일을 찾을 수 없습니다: 존재하지않는파일.txt", error());
    }

    // ── Cycle 3: 런타임 에러 ─────────────────────────────────────────────

    @Test
    @DisplayName("[run] FAIL - 런타임 에러가 발생하면 에러 메시지를 출력하고 exit code 70을 반환한다")
    void run_FAIL_런타임_에러가_발생하면_exit_code_70을_반환한다(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path file = tempDir.resolve("error.txt");
        Files.writeString(file, "print 1 / 0;");

        // Act
        int exitCode = runner.run(file.toString());

        // Assert
        assertEquals(70, exitCode);
        assertTrue(error().contains("0으로 나눌 수 없습니다"));
    }
}
