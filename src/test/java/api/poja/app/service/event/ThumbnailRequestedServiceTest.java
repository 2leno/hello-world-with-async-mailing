package api.poja.app.service.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import api.poja.app.endpoint.event.model.ThumbnailRequested;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.mail.Mailer;
import api.poja.app.repository.JSubmissionRepository;
import api.poja.app.repository.model.JSubmission;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ThumbnailRequestedServiceTest {

  @Mock private JSubmissionRepository submissionRepository;
  @Mock private BucketComponent bucketComponent;
  @Mock private Mailer mailer;

  private ThumbnailRequestedService service;

  @BeforeEach
  void setUp() {
    service = new ThumbnailRequestedService(submissionRepository, bucketComponent, mailer);
  }

  @Test
  void acceptProcessesThumbnailAndSendsEmail() throws Exception {
    String submissionId = UUID.randomUUID().toString();
    File originalFile = createTempPng();
    when(bucketComponent.download("originals/" + submissionId + ".png")).thenReturn(originalFile);
    when(submissionRepository.findById(UUID.fromString(submissionId)))
        .thenReturn(
            Optional.of(
                JSubmission.builder()
                    .id(UUID.fromString(submissionId))
                    .email("test@example.com")
                    .build()));
    when(bucketComponent.presign(any(), any(Duration.class)))
        .thenReturn(new URL("https://example.com/thumbnail.png"));

    ThumbnailRequested event =
        ThumbnailRequested.builder()
            .submissionId(submissionId)
            .email("test@example.com")
            .originalImageKey("originals/" + submissionId + ".png")
            .build();

    assertDoesNotThrow(() -> service.accept(event));

    verify(bucketComponent).upload(any(File.class), eq("thumbnails/" + submissionId + ".png"));
    verify(submissionRepository).save(any(JSubmission.class));
    verify(mailer).accept(any());

    originalFile.delete();
  }

  private File createTempPng() throws IOException {
    BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    File tempFile = File.createTempFile("test-", ".png");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);
    java.nio.file.Files.write(tempFile.toPath(), baos.toByteArray());
    return tempFile;
  }
}
