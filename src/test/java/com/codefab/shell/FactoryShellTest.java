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

@DisplayName("FactoryShell 테스트")
class FactoryShellTest {

    private ByteArrayOutputStream outStream;
    private ByteArrayOutputStream errStream;
    private FactoryShell shell;

    @BeforeEach
    void setUp() {
        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();
        shell = new FactoryShell(
                new PrintStream(outStream, true, StandardCharsets.UTF_8),
                new PrintStream(errStream, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return outStream.toString(StandardCharsets.UTF_8).trim().replace("\r\n", "\n");
    }

    private String error() {
        return errStream.toString(StandardCharsets.UTF_8).trim().replace("\r\n", "\n");
    }

    // ── Cycle 4: run <file> ───────────────────────────────────────────────

    @Test
    @DisplayName("[dispatch] PASS - 'run <file>' 명령은 파일을 실행하고 exit code 0을 반환한다")
    void dispatch_PASS_run_파일을_실행하고_exit_code_0을_반환한다(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "print 42;");

        // Act
        int exitCode = shell.dispatch(new String[]{"run", file.toString()});

        // Assert
        assertEquals(0, exitCode);
        assertEquals("42", output());
    }

    // ── Cycle 5: <file> 단축형 ───────────────────────────────────────────

    @Test
    @DisplayName("[dispatch] PASS - 파일 경로만 인자로 주면 run의 단축형으로 실행된다")
    void dispatch_PASS_파일경로만_주면_단축형으로_실행된다(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "print 99;");

        // Act
        int exitCode = shell.dispatch(new String[]{file.toString()});

        // Assert
        assertEquals(0, exitCode);
        assertEquals("99", output());
    }

    // ── Cycle 6: run 파일 경로 누락 ──────────────────────────────────────

    @Test
    @DisplayName("[dispatch] FAIL - 'run' 명령에 파일 경로가 없으면 에러 메시지를 출력하고 exit code 64를 반환한다")
    void dispatch_FAIL_run_파일_경로_누락시_exit_code_64를_반환한다() throws Exception {
        // Act
        int exitCode = shell.dispatch(new String[]{"run"});

        // Assert
        assertEquals(64, exitCode);
        assertTrue(error().contains("파일 경로"));
    }
}
