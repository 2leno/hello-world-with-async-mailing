# poja-async-mailing-template — async email workers for Spring Boot

A [Poja](https://poja.io) starter template with **SQS-powered async email sending** pre-configured. Define an event, wire a consumer, push — no queue infrastructure to manage.

→ **[Full guide on docs.poja.io](https://docs.poja.io/docs/hello-world-but-with-asynchronous-reply-by-email)**

Or hit the `Deploy to Poja` button to **deploy this template on your account** : 


[![Deploy on Poja](https://img.shields.io/badge/Deploy%20On%20Poja-007BFF?style=for-the-badge)](https://console.poja.io/applications/create/clone/?templateId=84df308f-8da6-4b70-a83f-0b146b1b8e5f)

---

### What you get

Two classes to write. Poja handles the queue, the worker, and the retries.

```java
// 1. The event — in endpoint.event.model
public class SendEmailRequested extends PojaEvent {
  private String to;

  @Override public Duration maxConsumerDuration() { return Duration.ofSeconds(45); }
  @Override public Duration maxConsumerBackoffBetweenRetries() { return Duration.ofSeconds(30); }
}

// 2. The consumer — in service.event (must be named {EventName}Service)
@Service @AllArgsConstructor
public class SendEmailRequestedService implements Consumer<SendEmailRequested> {
  private final Mailer mailer;

  @Override
  public void accept(SendEmailRequested event) {
    mailer.accept(new Email(new InternetAddress(event.getTo()),
        List.of(), List.of(), "Subject", "Body", List.of()));
  }
}
```

Produce the event from any controller — Poja routes it to the worker automatically.

> Part of the [Poja platform](https://poja.io) — deploy Spring Boot in minutes.
