package com.stock.forum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.forum.auth.AuthContext;
import com.stock.forum.common.ApiException;
import com.stock.forum.domain.MedicineEntity;
import com.stock.forum.domain.ReminderEntity;
import com.stock.forum.dto.ReminderDtos;
import com.stock.forum.repository.MedicineRepository;
import com.stock.forum.repository.ReminderRepository;
import com.stock.forum.util.DateTimes;
import com.stock.forum.util.JsonLists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ReminderService {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private final ReminderRepository reminderRepository;
    private final MedicineRepository medicineRepository;
    private final ObjectMapper objectMapper;

    public ReminderService(ReminderRepository reminderRepository, MedicineRepository medicineRepository, ObjectMapper objectMapper) {
        this.reminderRepository = reminderRepository;
        this.medicineRepository = medicineRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ReminderDtos.AddResponse add(ReminderDtos.AddRequest request) {
        Long userId = AuthGuard.requireSelf(request.userId);
        Long medicineId = AuthGuard.parseId(request.medicineId, "Invalid medicineId");
        medicineRepository.findByIdAndUserIdAndDeletedFalse(medicineId, userId)
                .orElseThrow(() -> ApiException.badRequest("Medicine not found"));

        ReminderEntity reminder = new ReminderEntity();
        reminder.setUserId(userId);
        reminder.setMedicineId(medicineId);
        reminder.setRemindTimes(JsonLists.toJson(objectMapper, normalizeTimes(request.remindTimes)));
        reminder.setRepeatType(normalizeRepeatType(request.repeatType));
        reminder.setStatus(normalizeStatus(request.status));
        reminder = reminderRepository.save(reminder);

        ReminderDtos.AddResponse response = new ReminderDtos.AddResponse();
        response.reminderId = String.valueOf(reminder.getId());
        return response;
    }

    @Transactional(readOnly = true)
    public List<ReminderDtos.ReminderItem> list(String userIdValue) {
        Long userId = AuthGuard.requireSelf(userIdValue);
        List<ReminderDtos.ReminderItem> items = new ArrayList<ReminderDtos.ReminderItem>();
        for (ReminderEntity reminder : reminderRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId)) {
            MedicineEntity medicine = medicineRepository.findByIdAndUserId(reminder.getMedicineId(), userId).orElse(null);
            items.add(toItem(reminder, medicine));
        }
        return items;
    }

    @Transactional
    public ReminderDtos.BooleanResponse update(ReminderDtos.UpdateRequest request) {
        Long userId = AuthContext.requireUserId();
        Long reminderId = AuthGuard.parseId(request.reminderId, "Invalid reminderId");
        ReminderEntity reminder = reminderRepository.findByIdAndUserIdAndDeletedFalse(reminderId, userId)
                .orElseThrow(() -> ApiException.badRequest("Reminder not found"));
        if (request.remindTimes != null) {
            reminder.setRemindTimes(JsonLists.toJson(objectMapper, normalizeTimes(request.remindTimes)));
        }
        if (request.repeatType != null) {
            reminder.setRepeatType(normalizeRepeatType(request.repeatType));
        }
        if (request.status != null) {
            reminder.setStatus(normalizeStatus(request.status));
        }
        reminderRepository.save(reminder);
        return success();
    }

    @Transactional
    public ReminderDtos.BooleanResponse delete(String reminderIdValue) {
        Long userId = AuthContext.requireUserId();
        Long reminderId = AuthGuard.parseId(reminderIdValue, "Invalid reminderId");
        ReminderEntity reminder = reminderRepository.findByIdAndUserIdAndDeletedFalse(reminderId, userId)
                .orElseThrow(() -> ApiException.badRequest("Reminder not found"));
        reminder.setStatus("disable");
        reminder.setDeleted(true);
        reminderRepository.save(reminder);
        return success();
    }

    public ReminderDtos.ReminderItem toItem(ReminderEntity reminder, MedicineEntity medicine) {
        ReminderDtos.ReminderItem item = new ReminderDtos.ReminderItem();
        item.reminderId = String.valueOf(reminder.getId());
        item.medicineId = String.valueOf(reminder.getMedicineId());
        item.medicineName = medicine == null ? "" : medicine.getMedicineName();
        item.remindTimes = JsonLists.fromJson(objectMapper, reminder.getRemindTimes());
        item.repeatType = reminder.getRepeatType();
        item.status = reminder.getStatus();
        item.createTime = DateTimes.format(reminder.getCreatedAt());
        return item;
    }

    private ReminderDtos.BooleanResponse success() {
        ReminderDtos.BooleanResponse response = new ReminderDtos.BooleanResponse();
        response.success = true;
        return response;
    }

    private List<String> normalizeTimes(List<String> times) {
        if (times == null || times.isEmpty()) {
            throw ApiException.badRequest("remindTimes is required");
        }
        List<String> result = new ArrayList<String>();
        for (String time : times) {
            try {
                result.add(LocalTime.parse(time, TIME_FORMAT).format(TIME_FORMAT));
            } catch (Exception ex) {
                throw ApiException.badRequest("Invalid remind time: " + time);
            }
        }
        Collections.sort(result);
        return result;
    }

    private String normalizeRepeatType(String repeatType) {
        if ("weekday".equals(repeatType)) {
            return "weekday";
        }
        if ("daily".equals(repeatType)) {
            return "daily";
        }
        throw ApiException.badRequest("Invalid repeatType");
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "enable";
        }
        if ("enable".equals(status) || "disable".equals(status)) {
            return status;
        }
        throw ApiException.badRequest("Invalid status");
    }
}
