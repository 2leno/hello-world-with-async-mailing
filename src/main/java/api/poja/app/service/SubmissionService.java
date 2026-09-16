package api.poja.app.service;

import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.mail.Email;
import api.poja.app.mail.Mailer;
import api.poja.app.model.Submission;
import api.poja.app.repository.JSubmissionRepository;
import api.poja.app.repository.model.JSubmission;
import jakarta.mail.internet.InternetAddress;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class SubmissionService {
  private final JSubmissionRepository submissionRepository;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  public Submission create(MultipartFile file, String email) throws IOException {
    validateImage(file);
    JSubmission submission = JSubmission.builder().email(email).thumbnailKey(null).build();
    submission = submissionRepository.save(submission);
    processThumbnailAsync(submission.getId().toString(), file, email);
    return toDto(submission);
  }

  public List<Submission> listAll() {
    return submissionRepository.findAll().stream().map(this::toDto).toList();
  }

  @Async
  @SneakyThrows
  void processThumbnailAsync(String id, MultipartFile file, String email) {
    File tempFile = File.createTempFile("submission-", ".tmp");
    file.transferTo(tempFile);

    BufferedImage originalImage = ImageIO.read(tempFile);
    BufferedImage thumbnail = resize(originalImage, 256, 256);

    String extension = getExtension(file.getOriginalFilename());
    String bucketKey = "thumbnails/" + id + "." + extension;
    File thumbnailFile = File.createTempFile("thumb-", "." + extension);
    ImageIO.write(thumbnail, extension, thumbnailFile);

    bucketComponent.upload(thumbnailFile, bucketKey);

    JSubmission submission = submissionRepository.findById(UUID.fromString(id)).orElseThrow();
    submission.setThumbnailKey(bucketKey);
    submissionRepository.save(submission);

    String downloadUrl = bucketComponent.presign(bucketKey, Duration.ofHours(1)).toString();
    InternetAddress recipientAddress = new InternetAddress(email);
    mailer.accept(
        new Email(
            recipientAddress,
            List.of(),
            List.of(),
            "Your thumbnail is ready",
            "<p>Your thumbnail is ready for download: <a href=\""
                + downloadUrl
                + "\">Download</a></p>",
            List.of()));

    tempFile.delete();
    thumbnailFile.delete();
  }

  private void validateImage(MultipartFile file) {
    String contentType = file.getContentType();
    if (!"image/png".equals(contentType) && !"image/jpeg".equals(contentType)) {
      throw new IllegalArgumentException("Invalid file format: only PNG and JPEG are supported.");
    }
  }

  private BufferedImage resize(BufferedImage original, int width, int height) {
    BufferedImage resized = new BufferedImage(width, height, original.getType());
    var g = resized.createGraphics();
    g.drawImage(original, 0, 0, width, height, null);
    g.dispose();
    return resized;
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
