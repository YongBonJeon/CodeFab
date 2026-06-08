package com.codefab.shell;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

    // ── Cycle 12: next ────────────────────────────────────────────────────

    @Test
    @DisplayName("[Debugger] PASS - next 명령은 블록 내부에 진입하지 않고 다음 문장으로 이동한다")
    void debugger_PASS_next_명령은_블록_내부에_진입하지_않는다() {
        // line 1: var x = 1;
        // line 2: {
        // line 3:   var y = 2;   ← next 모드에서 이 줄은 정지하지 않아야 함
        // line 4: }
        // line 5: print x;
        String source = "var x = 1;\n{\nvar y = 2;\n}\nprint x;";
        String commands = "next\nnext\nstep\n";
        String output = debug(source, commands);

        assertTrue(output.contains("[DEBUG] 1번째 줄에서 정지"));
        assertTrue(output.contains("[DEBUG] 2번째 줄에서 정지"));
        assertTrue(output.contains("[DEBUG] 5번째 줄에서 정지"));
        assertFalse(output.contains("[DEBUG] 3번째 줄에서 정지"));
    }

    // ── Cycle 13: remove ──────────────────────────────────────────────────

    @Test
    @DisplayName("[Debugger] PASS - remove 명령으로 breakpoint를 해제하면 해당 줄에서 정지하지 않는다")
    void debugger_PASS_remove_명령으로_breakpoint를_해제하면_정지하지_않는다() {
        String source = "var x = 1;\nvar y = 2;\nprint x;";
        // 1번 줄 정지 → break 3 설정 → remove 3 해제 → continue → 정지 없이 종료
        String commands = "break 3\nremove 3\ncontinue\n";
        String output = debug(source, commands);

        assertFalse(output.contains("[DEBUG] 3번째 줄에서 정지"));
        assertTrue(output.contains("[DEBUG] 실행 종료"));
    }

    // ── Cycle 14: breakpoints 목록 ───────────────────────────────────────

    @Test
    @DisplayName("[Debugger] PASS - breakpoints 명령은 설정된 breakpoint 목록을 출력한다")
    void debugger_PASS_breakpoints_명령은_설정된_목록을_출력한다() {
        String source = "var x = 1;\nvar y = 2;\nprint x;";
        // 1번 줄 정지 → break 2, break 3 설정 → breakpoints 조회 → continue
        String commands = "break 2\nbreak 3\nbreakpoints\ncontinue\nstep\nstep\n";
        String output = debug(source, commands);

        assertTrue(output.contains("[DEBUG] breakpoints: [2, 3]"));
    }

    // ── Cycle 15: unwatch ─────────────────────────────────────────────────

    @Test
    @DisplayName("[Debugger] PASS - unwatch 명령으로 감시를 해제하면 이후 정지 시 값이 출력되지 않는다")
    void debugger_PASS_unwatch_명령으로_감시_해제하면_값이_출력되지_않는다() {
        String source = "var x = 1;\nvar y = 2;\nprint x;";
        // 1번 줄 정지 → watch x → unwatch x → step → 2번 줄 정지 시 x 값 출력 없음
        String commands = "watch x\nunwatch x\nstep\nstep\nstep\n";
        String output = debug(source, commands);

        assertTrue(output.contains("[WATCH] 'x' 감시 등록"));
        assertTrue(output.contains("[WATCH] 'x' 감시 해제"));
        assertFalse(output.contains("[WATCH] x = 1"));
    }

    // ── Cycle 16: watches 목록 ───────────────────────────────────────────

    @Test
    @DisplayName("[Debugger] PASS - watches 명령은 감시 중인 변수 목록과 값을 출력한다")
    void debugger_PASS_watches_명령은_감시_목록과_값을_출력한다() {
        String source = "var x = 1;\nvar y = 2;\nprint x;";
        // 1번 줄 정지 → watch x, watch y → step → 2번 줄 정지 → step → 3번 줄 정지 → watches 조회
        String commands = "watch x\nwatch y\nstep\nstep\nwatches\nstep\n";
        String output = debug(source, commands);

        assertTrue(output.contains("[WATCH] x = 1"));
        assertTrue(output.contains("[WATCH] y = 2"));
    }
}
