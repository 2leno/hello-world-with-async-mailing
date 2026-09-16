package api.poja.app.service.event;

import api.poja.app.endpoint.event.model.ThumbnailRequested;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.mail.Email;
import api.poja.app.mail.Mailer;
import api.poja.app.repository.JSubmissionRepository;
import api.poja.app.repository.model.JSubmission;
import jakarta.mail.internet.InternetAddress;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ThumbnailRequestedService implements Consumer<ThumbnailRequested> {
  private final JSubmissionRepository submissionRepository;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(ThumbnailRequested event) {
    File originalFile = bucketComponent.download(event.getOriginalImageKey());

    BufferedImage originalImage = ImageIO.read(originalFile);
    BufferedImage thumbnail = resize(originalImage, 256, 256);

    String extension = getExtension(event.getOriginalImageKey());
    String thumbnailKey = "thumbnails/" + event.getSubmissionId() + "." + extension;
    File thumbnailFile = File.createTempFile("thumb-", "." + extension);
    ImageIO.write(thumbnail, extension, thumbnailFile);

    bucketComponent.upload(thumbnailFile, thumbnailKey);

    JSubmission submission =
        submissionRepository.findById(UUID.fromString(event.getSubmissionId())).orElseThrow();
    submission.setThumbnailKey(thumbnailKey);
    submissionRepository.save(submission);

    String downloadUrl = bucketComponent.presign(thumbnailKey, Duration.ofHours(1)).toString();
    InternetAddress recipientAddress = new InternetAddress(event.getEmail());
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

    originalFile.delete();
    thumbnailFile.delete();
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
}
