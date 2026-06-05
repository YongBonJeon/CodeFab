package com.codefab.shell;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Debugger 테스트")
class DebuggerTest {

    private String debug(String source, String commands) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outStream, true, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(new StringReader(commands));
        new Debugger(source, in, out).run(source, "test.cfab");
        return outStream.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    // ── Cycle 8: step - 줄 번호 정지 메시지 ─────────────────────────────

    @Test
    @DisplayName("[Debugger] PASS - step 모드에서 각 문장 줄 번호에서 정지 메시지가 출력된다")
    void debugger_PASS_step_모드에서_줄번호_정지_메시지가_출력된다() {
        String source = "var x = 1;\nprint x;";
        String output = debug(source, "step\nstep\n");

        assertTrue(output.contains("[DEBUG] 1번째 줄에서 정지"));
        assertTrue(output.contains("[DEBUG] 2번째 줄에서 정지"));
    }
}
