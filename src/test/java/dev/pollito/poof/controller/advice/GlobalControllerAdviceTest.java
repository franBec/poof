package dev.pollito.poof.controller.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

@ExtendWith(MockitoExtension.class)
class GlobalControllerAdviceTest {
  @InjectMocks GlobalControllerAdvice globalControllerAdvice;

  @Test
  void exceptionIsHandled() {
    RuntimeException runtimeException = new RuntimeException("Mock RuntimeException message");
    ProblemDetail adviceResponse = globalControllerAdvice.handle(runtimeException);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), adviceResponse.getStatus());
    assertEquals(runtimeException.getLocalizedMessage(), adviceResponse.getDetail());
    assertNotNull(adviceResponse.getProperties());
    assertNotNull(adviceResponse.getProperties().get("trace"));

    String timestamp = (String) adviceResponse.getProperties().get("timestamp");
    assertNotNull(timestamp);
    assertTrue(isValidISOInstant(timestamp));
  }

  private boolean isValidISOInstant(String dateTimeString) {
    Instant.parse(dateTimeString);
    return true;
  }
}
