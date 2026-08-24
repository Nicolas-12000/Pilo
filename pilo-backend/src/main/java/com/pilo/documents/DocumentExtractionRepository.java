package com.pilo.documents;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentExtractionRepository extends JpaRepository<DocumentExtraction, UUID> {
}
