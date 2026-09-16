package api.poja.app.service.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import api.poja.app.endpoint.event.model.SendEmailRequested;
import api.poja.app.mail.Mailer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendEmailRequestedServiceTest {

  @Mock private Mailer mailer;

  private SendEmailRequestedService service;

  @BeforeEach
  void setUp() {
    service = new SendEmailRequestedService(mailer);
  }

  @Test
  void acceptSendsEmail() {
    SendEmailRequested event = SendEmailRequested.builder().to("test@example.com").build();

    assertDoesNotThrow(() -> service.accept(event));

    verify(mailer).accept(any());
  }
}
