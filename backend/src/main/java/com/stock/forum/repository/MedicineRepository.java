package com.stock.forum.repository;

import com.stock.forum.domain.MedicineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<MedicineEntity, Long> {
    List<MedicineEntity> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    List<MedicineEntity> findByUserIdAndDeletedFalse(Long userId);

    Optional<MedicineEntity> findByIdAndUserId(Long id, Long userId);

    Optional<MedicineEntity> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);
}
