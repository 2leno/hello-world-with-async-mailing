package api.poja.app.endpoint.event;

import static java.lang.System.getenv;

import api.poja.app.PojaGenerated;
import lombok.Getter;

@PojaGenerated
public enum EventStack {
  EVENT_STACK_1(getenv("AWS_EVENT_STACK_1_SQS_QUEUE_URL")),
  EVENT_STACK_2(getenv("AWS_EVENT_STACK_2_SQS_QUEUE_URL"));

  @Getter private final String sqsQueueUrl;

  EventStack(String sqsQueueUrl) {
    this.sqsQueueUrl = sqsQueueUrl;
  }
}
