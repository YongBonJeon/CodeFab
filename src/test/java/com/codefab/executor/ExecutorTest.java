package com.codefab.executor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Executor Unit 테스트")
class ExecutorTest {

  private ByteArrayOutputStream outputStream;
  private Executor executor;

  @BeforeEach
  void setUp() {
    outputStream = new ByteArrayOutputStream();
    executor = new Executor(new PrintStream(outputStream));
  }

  private String output() {
    return outputStream.toString().trim();
  }
}
