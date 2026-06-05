package com.codefab.optimizer;

import static org.assertj.core.api.Assertions.assertThat;

import com.codefab.assembler.Parser;
import com.codefab.assembler.Tokenizer;
import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.executor.Executor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("상수 연산 최적화 (수식 합치기)")
class OptimizerTest {

  private List<Stmt> parse(String source) {
    return new Parser(new Tokenizer(source).tokenize()).parse();
  }

  /**
   * Test Double (spy): an executor that counts how many binary operations it actually evaluates at
   * runtime. Lets us verify the fold reduced the runtime computation count to zero.
   */
  private static class CountingExecutor extends Executor {
    int binaryEvaluations = 0;

    CountingExecutor(PrintStream out) {
      super(out);
    }

    @Override
    public Object visitBinary(Expr.Binary expr) {
      binaryEvaluations++;
      return super.visitBinary(expr);
    }
  }

  private CountingExecutor newExecutor(ByteArrayOutputStream out) {
    return new CountingExecutor(new PrintStream(out, true, StandardCharsets.UTF_8));
  }

  @Test
  @DisplayName("상수만으로 이루어진 식은 단일 리터럴로 접힌다")
  void foldsConstantExpressionToLiteral() {
    List<Stmt> stmts = new Optimizer().optimize(parse("print 1 + 2 * 3;"));

    Expr folded = ((Stmt.Print) stmts.get(0)).expression;
    assertThat(folded).isInstanceOf(Expr.Literal.class);
    assertThat(((Expr.Literal) folded).value).isEqualTo(7.0);
  }

  @Test
  @DisplayName("PDF 예시: 복잡한 상수식이 리터럴 5 로 접힌다")
  void foldsPdfExampleToFive() {
    String src = "print (1 - 2 * 3 * 4 * 5 / 6 + 7 + 8 + 9) % 1000 % 30;";

    List<Stmt> stmts = new Optimizer().optimize(parse(src));
    Expr folded = ((Stmt.Print) stmts.get(0)).expression;

    assertThat(folded).isInstanceOf(Expr.Literal.class);
    assertThat(((Expr.Literal) folded).value).isEqualTo(5.0);
  }

  @Test
  @DisplayName("Test Double 검증: 계산 횟수가 N 회에서 0 회로 줄어든다")
  void computationCountDropsToZero() {
    String src = "print (1 - 2 * 3 * 4 * 5 / 6 + 7 + 8 + 9) % 1000 % 30;";

    // Before optimization: the executor evaluates every binary operator.
    ByteArrayOutputStream beforeOut = new ByteArrayOutputStream();
    CountingExecutor before = newExecutor(beforeOut);
    before.execute(parse(src));
    int countBefore = before.binaryEvaluations;
    assertThat(countBefore).isGreaterThan(0);

    // After optimization: the whole expression is a literal -> zero binary evaluations.
    Optimizer optimizer = new Optimizer();
    List<Stmt> optimized = optimizer.optimize(parse(src));
    ByteArrayOutputStream afterOut = new ByteArrayOutputStream();
    CountingExecutor after = newExecutor(afterOut);
    after.execute(optimized);

    assertThat(after.binaryEvaluations).isZero();
    assertThat(optimizer.getFoldCount()).isEqualTo(countBefore); // N folds replaced N runtime ops
    assertThat(beforeOut.toString(StandardCharsets.UTF_8).trim()).isEqualTo("5");
    assertThat(afterOut.toString(StandardCharsets.UTF_8).trim()).isEqualTo("5");
  }

  @Test
  @DisplayName("변수가 포함된 식은 접지 않는다")
  void doesNotFoldExpressionWithVariable() {
    List<Stmt> stmts = new Optimizer().optimize(parse("print a + 1;"));
    Expr expr = ((Stmt.Print) stmts.get(0)).expression;
    assertThat(expr).isInstanceOf(Expr.Binary.class);
  }

  @Test
  @DisplayName("0 으로 나누는 상수식은 접지 않고 런타임으로 넘긴다")
  void doesNotFoldDivisionByZero() {
    List<Stmt> stmts = new Optimizer().optimize(parse("print 1 / 0;"));
    Expr expr = ((Stmt.Print) stmts.get(0)).expression;
    assertThat(expr).isInstanceOf(Expr.Binary.class);
  }
}
