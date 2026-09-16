package api.poja.app.model;

import java.time.Instant;

public record Submission(String id, String email, String thumbnailKey, Instant createdAt) {}
