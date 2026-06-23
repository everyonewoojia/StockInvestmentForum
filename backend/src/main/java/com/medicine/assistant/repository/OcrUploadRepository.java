package com.medicine.assistant.repository;

import com.medicine.assistant.domain.OcrUploadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OcrUploadRepository extends JpaRepository<OcrUploadEntity, Long> {
    Optional<OcrUploadEntity> findByStoredName(String storedName);

    Optional<OcrUploadEntity> findByStoredNameAndUserId(String storedName, Long userId);
}
