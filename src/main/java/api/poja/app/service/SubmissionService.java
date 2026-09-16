package api.poja.app.service;

import api.poja.app.endpoint.event.EventProducer;
import api.poja.app.endpoint.event.model.ThumbnailRequested;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.model.Submission;
import api.poja.app.repository.JSubmissionRepository;
import api.poja.app.repository.model.JSubmission;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class SubmissionService {
  private final JSubmissionRepository submissionRepository;
  private final BucketComponent bucketComponent;
  private final EventProducer<ThumbnailRequested> eventProducer;

  public Submission create(MultipartFile file, String email) throws IOException {
    validateImage(file);
    JSubmission submission = JSubmission.builder().email(email).thumbnailKey(null).build();
    submission = submissionRepository.save(submission);

    File tempFile = File.createTempFile("submission-", ".tmp");
    file.transferTo(tempFile);
    String extension = getExtension(file.getOriginalFilename());
    String originalKey = "originals/" + submission.getId() + "." + extension;
    bucketComponent.upload(tempFile, originalKey);
    tempFile.delete();

    var event =
        ThumbnailRequested.builder()
            .submissionId(submission.getId().toString())
            .email(email)
            .originalImageKey(originalKey)
            .build();
    eventProducer.accept(List.of(event));

    return toDto(submission);
  }

  public List<Submission> listAll() {
    return submissionRepository.findAll().stream().map(this::toDto).toList();
  }

  private void validateImage(MultipartFile file) {
    String contentType = file.getContentType();
    if (!"image/png".equals(contentType) && !"image/jpeg".equals(contentType)) {
      throw new IllegalArgumentException("Invalid file format: only PNG and JPEG are supported.");
    }
  }

  private String getExtension(String filename) {
    if (filename == null) return "png";
    int dotIndex = filename.lastIndexOf('.');
    return dotIndex >= 0 ? filename.substring(dotIndex + 1) : "png";
  }

  private Submission toDto(JSubmission entity) {
    return new Submission(
        entity.getId().toString(),
        entity.getEmail(),
        entity.getThumbnailKey(),
        entity.getCreatedAt());
  }
}
