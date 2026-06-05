package com.codefab.shell;

import com.codefab.CodeFab;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("PromptShell 멀티라인 테스트")
class PromptShellTest {

    private String runWith(String input) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outStream, true, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(new StringReader(input));
        CodeFab fab = new CodeFab(out, out);
        new PromptShell(fab, reader, out).run();
        return outStream.toString(StandardCharsets.UTF_8)
                .replace("\r\n", "\n")
                .replaceAll("CodeFab[^\n]*\n", "")  // 배너 라인 제거
                .replaceAll("\\([^\n]*\\)\n", "")   // 안내 메시지 라인 제거
                .replace(">>> ", "")                 // >>> 프롬프트 제거
                .replace("... ", "")                 // ... 프롬프트 제거
                .trim();
    }

    // ── Cycle 7: 멀티라인 버퍼 실행 ──────────────────────────────────────

    @Test
    @DisplayName("[run] PASS - 여러 줄 입력 후 빈 줄을 입력하면 버퍼에 쌓인 코드를 실행한다")
    void run_PASS_빈줄_입력시_버퍼를_실행한다() throws Exception {
        // "var x = 10;\nprint x;\n" → 빈 줄로 실행 → exit
        String output = runWith("var x = 10;\nprint x;\n\nexit\n");
        assertEquals("10", output);
    }
}
