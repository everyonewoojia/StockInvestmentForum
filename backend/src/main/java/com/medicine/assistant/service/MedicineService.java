package com.medicine.assistant.service;

import com.medicine.assistant.auth.AuthContext;
import com.medicine.assistant.common.ApiException;
import com.medicine.assistant.domain.MedicineEntity;
import com.medicine.assistant.domain.ReminderEntity;
import com.medicine.assistant.dto.MedicineDtos;
import com.medicine.assistant.dto.OcrDtos;
import com.medicine.assistant.repository.MedicineRepository;
import com.medicine.assistant.repository.ReminderRepository;
import com.medicine.assistant.util.DateTimes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MedicineService {
    private final MedicineRepository medicineRepository;
    private final ReminderRepository reminderRepository;

    public MedicineService(MedicineRepository medicineRepository, ReminderRepository reminderRepository) {
        this.medicineRepository = medicineRepository;
        this.reminderRepository = reminderRepository;
    }

    @Transactional
    public OcrDtos.MedicineSaveResponse createFromOcr(OcrDtos.MedicineSaveRequest request) {
        Long userId = AuthGuard.requireSelf(request.userId);
        validateMedicine(request.medicineName, request.dailyTimes, request.dose, request.takeTime, request.cycle);

        MedicineEntity medicine = new MedicineEntity();
        medicine.setUserId(userId);
        medicine.setMedicineName(request.medicineName.trim());
        medicine.setDailyTimes(request.dailyTimes);
        medicine.setDose(request.dose.trim());
        medicine.setTakeTime(normalizeTakeTime(request.takeTime));
        medicine.setCycleDays(request.cycle);
        medicine = medicineRepository.save(medicine);

        OcrDtos.MedicineSaveResponse response = new OcrDtos.MedicineSaveResponse();
        response.medicineId = String.valueOf(medicine.getId());
        return response;
    }

    @Transactional(readOnly = true)
    public List<MedicineDtos.MedicineItem> list(String userIdValue) {
        Long userId = AuthGuard.requireSelf(userIdValue);
        List<MedicineDtos.MedicineItem> items = new ArrayList<MedicineDtos.MedicineItem>();
        for (MedicineEntity medicine : medicineRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId)) {
            items.add(toItem(medicine));
        }
        return items;
    }

    @Transactional
    public MedicineDtos.DeleteResponse delete(String medicineIdValue) {
        Long userId = AuthContext.requireUserId();
        Long medicineId = AuthGuard.parseId(medicineIdValue, "Invalid medicineId");
        MedicineEntity medicine = medicineRepository.findByIdAndUserIdAndDeletedFalse(medicineId, userId)
                .orElseThrow(() -> ApiException.badRequest("Medicine not found"));
        medicine.setDeleted(true);
        medicineRepository.save(medicine);

        for (ReminderEntity reminder : reminderRepository.findByMedicineIdAndDeletedFalse(medicineId)) {
            if (userId.equals(reminder.getUserId())) {
                reminder.setStatus("disable");
                reminder.setDeleted(true);
                reminderRepository.save(reminder);
            }
        }

        MedicineDtos.DeleteResponse response = new MedicineDtos.DeleteResponse();
        response.success = true;
        return response;
    }

    public MedicineDtos.MedicineItem toItem(MedicineEntity medicine) {
        MedicineDtos.MedicineItem item = new MedicineDtos.MedicineItem();
        item.medicineId = String.valueOf(medicine.getId());
        item.medicineName = medicine.getMedicineName();
        item.dailyTimes = medicine.getDailyTimes();
        item.dose = medicine.getDose();
        item.takeTime = medicine.getTakeTime();
        item.cycle = medicine.getCycleDays();
        item.createTime = DateTimes.format(medicine.getCreatedAt());
        return item;
    }

    private void validateMedicine(String name, Integer dailyTimes, String dose, String takeTime, Integer cycle) {
        if (name == null || name.trim().isEmpty()) {
            throw ApiException.badRequest("medicineName is required");
        }
        if (dailyTimes == null || dailyTimes <= 0) {
            throw ApiException.badRequest("dailyTimes must be positive");
        }
        if (dose == null || dose.trim().isEmpty()) {
            throw ApiException.badRequest("dose is required");
        }
        if (takeTime == null || takeTime.trim().isEmpty()) {
            throw ApiException.badRequest("takeTime is required");
        }
        if (cycle == null || cycle <= 0) {
            throw ApiException.badRequest("cycle must be positive");
        }
    }

    private String normalizeTakeTime(String takeTime) {
        if ("饭前".equals(takeTime) || "饭后".equals(takeTime)) {
            return takeTime;
        }
        return "饭后";
    }
}
