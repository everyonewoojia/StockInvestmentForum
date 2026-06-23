package com.medicine.assistant.repository;

import com.medicine.assistant.domain.ReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReminderRepository extends JpaRepository<ReminderEntity, Long> {
    List<ReminderEntity> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    List<ReminderEntity> findByUserIdAndDeletedFalseAndStatus(Long userId, String status);

    List<ReminderEntity> findByMedicineIdAndDeletedFalse(Long medicineId);

    Optional<ReminderEntity> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);
}
