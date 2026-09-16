package api.poja.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import api.poja.app.conf.FacadeIT;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.mail.Mailer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class SubmissionIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;

  @MockBean private BucketComponent bucketComponent;

  @MockBean private Mailer mailer;

  private static final String BASE_URL = "/submissions";

  @Test
  void createSubmissionWithValidPngReturns201AndThumbnailKeyNull() throws Exception {
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

    var response = restTemplate.exchange(BASE_URL, HttpMethod.POST, request, Object.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
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

    var response = restTemplate.exchange(BASE_URL, HttpMethod.POST, request, Object.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void listSubmissionsReturns200WithArray() {
    var response = restTemplate.exchange(BASE_URL, HttpMethod.GET, null, List.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void createSubmissionPersistsInDatabase() throws Exception {
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

    restTemplate.exchange(BASE_URL, HttpMethod.POST, request, Object.class);

    var listResponse = restTemplate.exchange(BASE_URL, HttpMethod.GET, null, List.class);

    assertEquals(HttpStatus.OK, listResponse.getStatusCode());
    assertTrue(((List<?>) listResponse.getBody()).size() > 0);
  }

  private byte[] createTestPng() throws Exception {
    BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);
    return baos.toByteArray();
  }
}
