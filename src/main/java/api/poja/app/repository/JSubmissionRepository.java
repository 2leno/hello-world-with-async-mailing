package api.poja.app.repository;

import api.poja.app.repository.model.JSubmission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JSubmissionRepository extends JpaRepository<JSubmission, UUID> {
  @Override
  List<JSubmission> findAll();
}
