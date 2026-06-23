package com.medicine.assistant.repository;

import com.medicine.assistant.domain.RecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {
    Optional<RecordEntity> findByUserIdAndMedicineIdAndPlanTime(Long userId, Long medicineId, LocalDateTime planTime);

    List<RecordEntity> findByUserIdAndPlanTimeBetweenOrderByPlanTimeAsc(Long userId, LocalDateTime start, LocalDateTime end);
}
