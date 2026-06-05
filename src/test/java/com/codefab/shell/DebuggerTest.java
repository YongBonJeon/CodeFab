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
        new Debugger(source, in, out).run(source, "test.txt");
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

    // ── Cycle 9: break + continue ─────────────────────────────────────────

    @Test
    @DisplayName("[Debugger] PASS - breakpoint 설정 후 continue하면 해당 줄에서 정지한다")
    void debugger_PASS_breakpoint_설정_후_continue하면_해당_줄에서_정지한다() {
        String source = "var x = 1;\nvar y = 2;\nprint x;";
        // 1번 줄에서 정지 → break 3 설정 → continue → 3번 줄 breakpoint에서 정지 → step
        String commands = "break 3\ncontinue\nstep\n";
        String output = debug(source, commands);

        assertTrue(output.contains("[DEBUG] 3번째 줄에서 정지"));
        assertTrue(output.contains("(breakpoint)"));
    }

    // ── Cycle 10: watch ───────────────────────────────────────────────────

    @Test
    @DisplayName("[Debugger] PASS - watch 명령으로 변수를 감시하면 각 정지 시 값이 출력된다")
    void debugger_PASS_watch_명령으로_변수를_감시하면_값이_출력된다() {
        String source = "var x = 1;\nvar y = 2;\nprint x;";
        // 1번 줄 정지 → watch x 등록 → step → 2번 줄 정지(WATCH x = 1 출력) → step → step
        String commands = "watch x\nstep\nstep\nstep\n";
        String output = debug(source, commands);

        assertTrue(output.contains("[WATCH] 'x' 감시 등록"));
        assertTrue(output.contains("[WATCH] x = 1"));
    }

    // ── Cycle 11: inspect ─────────────────────────────────────────────────

    @Test
    @DisplayName("[Debugger] PASS - inspect 명령은 현재 스코프의 변수와 값을 출력한다")
    void debugger_PASS_inspect_명령은_현재_스코프_변수를_출력한다() {
        String source = "var x = 1;\nvar y = 2;\nprint x;";
        // 1번 줄 정지 → step → 2번 줄 정지 → step → 3번 줄 정지 → inspect → step
        String commands = "step\nstep\ninspect\nstep\n";
        String output = debug(source, commands);

        assertTrue(output.contains("x = 1"));
        assertTrue(output.contains("y = 2"));
    }
}
