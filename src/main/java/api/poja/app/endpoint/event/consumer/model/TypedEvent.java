package api.poja.app.endpoint.event.consumer.model;

import api.poja.app.PojaGenerated;
import api.poja.app.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
