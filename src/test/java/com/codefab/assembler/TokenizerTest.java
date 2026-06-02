package com.codefab.assembler;

import static com.codefab.token.TokenType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codefab.error.ParseError;
import com.codefab.token.Token;
import com.codefab.token.TokenType;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenizerTest {

    private List<TokenType> types(String source) {
        return new Tokenizer(source).tokenize().stream()
                .map(t -> t.type)
                .collect(Collectors.toList());
    }

    @Test
    @DisplayName("구분자와 괄호를 각각의 토큰으로 분해한다")
    void tokenizesDelimiters() {
        assertThat(types("(){};"))
                .containsExactly(LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, SEMICOLON, EOF);
    }

    @Test
    @DisplayName("공백/탭/캐리지리턴은 토큰으로 만들지 않는다")
    void ignoresWhitespace() {
        assertThat(types("(  )\t{ }")).containsExactly(
                LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, EOF);
    }

    @Test
    @DisplayName("토큰 스트림의 마지막에는 항상 EOF 토큰이 추가된다")
    void appendsEofAtEnd() {
        List<Token> tokens = new Tokenizer("").tokenize();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).type).isEqualTo(EOF);
    }

    @Test
    @DisplayName("개행을 만나면 라인 번호를 증가시킨다")
    void tracksLineNumber() {
        List<Token> tokens = new Tokenizer("(\n)").tokenize();
        assertThat(tokens.get(0).line).isEqualTo(1);
        assertThat(tokens.get(1).line).isEqualTo(2);
    }

    @Test
    @DisplayName("정의되지 않은 문자를 만나면 ParseError 를 던진다")
    void throwsOnUnexpectedCharacter() {
        assertThatThrownBy(() -> new Tokenizer("@").tokenize())
                .isInstanceOf(ParseError.class)
                .hasMessageContaining("예상치 못한 문자");
    }

    @Test
    @DisplayName("산술/비교/단항 연산자를 각각의 토큰으로 분해한다")
    void tokenizesOperators() {
        assertThat(types("+-*/=><!")).containsExactly(
                PLUS, MINUS, STAR, SLASH, EQUAL, GREATER, LESS, BANG, EOF);
    }

    @Test
    @DisplayName("// 주석은 줄 끝까지 무시한다")
    void ignoresLineComment() {
        assertThat(types("+ // 주석은 토큰이 아니다\n-"))
                .containsExactly(PLUS, MINUS, EOF);
    }

    @Test
    @DisplayName("단일 슬래시는 SLASH 토큰으로 분해한다")
    void singleSlashIsDivision() {
        assertThat(types("/")).containsExactly(SLASH, EOF);
    }

    @Test
    @DisplayName("정수 리터럴을 NUMBER 토큰으로 분해하고 double 값을 보관한다")
    void tokenizesIntegerLiteral() {
        Token number = new Tokenizer("37").tokenize().get(0);
        assertThat(number.type).isEqualTo(NUMBER);
        assertThat(number.origin).isEqualTo("37");
        assertThat(number.literal).isEqualTo(37.0);
    }

    @Test
    @DisplayName("소수점 리터럴을 하나의 NUMBER 토큰으로 분해한다")
    void tokenizesDecimalLiteral() {
        Token number = new Tokenizer("3.14").tokenize().get(0);
        assertThat(number.type).isEqualTo(NUMBER);
        assertThat(number.literal).isEqualTo(3.14);
    }
}
