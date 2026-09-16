package api.poja.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import api.poja.app.conf.FacadeIT;
import api.poja.app.endpoint.event.EventProducer;
import api.poja.app.endpoint.event.model.ThumbnailRequested;
import api.poja.app.file.bucket.BucketComponent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class SubmissionIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;

  @MockBean private BucketComponent bucketComponent;

  @MockBean private EventProducer<ThumbnailRequested> eventProducer;

  private static final String BASE_URL = "/submissions";

  @Test
  void createSubmissionWithValidPngReturns201() throws Exception {
    byte[] pngBytes = createTestPng();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new ByteArrayResource(pngBytes) {
          @Override
          public String getFilename() {
            return "test.png";
          }
        });
    body.add("email", "user@example.com");

    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

    ResponseEntity<String> response =
        restTemplate.exchange(BASE_URL, HttpMethod.POST, request, String.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("user@example.com"));
    verify(eventProducer).accept(any());
  }

  @Test
  void createSubmissionWithInvalidFileTypeReturns400() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new ByteArrayResource("not an image".getBytes()) {
          @Override
          public String getFilename() {
            return "test.txt";
          }
        });
    body.add("email", "user@example.com");

    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

    ResponseEntity<String> response =
        restTemplate.exchange(BASE_URL, HttpMethod.POST, request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void listSubmissionsReturns200() {
    ResponseEntity<String> response =
        restTemplate.exchange(BASE_URL, HttpMethod.GET, null, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void createSubmissionPersistsAndIsListed() throws Exception {
    byte[] pngBytes = createTestPng();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new ByteArrayResource(pngBytes) {
          @Override
          public String getFilename() {
            return "test2.png";
          }
        });
    body.add("email", "persist@example.com");

    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

    ResponseEntity<String> createResponse =
        restTemplate.exchange(BASE_URL, HttpMethod.POST, request, String.class);
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

    ResponseEntity<String> listResponse =
        restTemplate.exchange(BASE_URL, HttpMethod.GET, null, String.class);
    assertEquals(HttpStatus.OK, listResponse.getStatusCode());
    assertTrue(listResponse.getBody().length() > 2);
  }

  private byte[] createTestPng() throws Exception {
    BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);
    return baos.toByteArray();
  }
}
