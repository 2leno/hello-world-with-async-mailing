package api.poja.app.endpoint.rest.controller;

import api.poja.app.model.ErrorResponse;
import api.poja.app.model.Submission;
import api.poja.app.service.SubmissionService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/submissions")
@AllArgsConstructor
public class SubmissionController {
  private final SubmissionService submissionService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> create(
      @RequestParam("file") MultipartFile file, @RequestParam("email") String email) {
    try {
      Submission submission = submissionService.create(file, email);
      return ResponseEntity.status(201).body(submission);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(new ErrorResponse("Internal server error."));
    }
  }

  @GetMapping
  public ResponseEntity<?> listAll() {
    try {
      List<Submission> submissions = submissionService.listAll();
      return ResponseEntity.ok(submissions);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(new ErrorResponse("Internal server error."));
    }
  }
}
