package com.medicine.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.assistant.domain.MedicineEntity;
import com.medicine.assistant.domain.ReminderEntity;
import com.medicine.assistant.repository.MedicineRepository;
import com.medicine.assistant.repository.ReminderRepository;
import com.medicine.assistant.util.JsonLists;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RecordPlanService {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private final ReminderRepository reminderRepository;
    private final MedicineRepository medicineRepository;
    private final ObjectMapper objectMapper;

    public RecordPlanService(ReminderRepository reminderRepository, MedicineRepository medicineRepository, ObjectMapper objectMapper) {
        this.reminderRepository = reminderRepository;
        this.medicineRepository = medicineRepository;
        this.objectMapper = objectMapper;
    }

    public List<PlanItem> buildPlans(Long userId, LocalDateTime start, LocalDateTime end) {
        List<PlanItem> plans = new ArrayList<PlanItem>();
        for (ReminderEntity reminder : reminderRepository.findByUserIdAndDeletedFalseAndStatus(userId, "enable")) {
            MedicineEntity medicine = medicineRepository.findByIdAndUserIdAndDeletedFalse(reminder.getMedicineId(), userId).orElse(null);
            if (medicine == null) {
                continue;
            }
            appendReminderPlans(plans, medicine, reminder, start, end);
        }
        plans.sort(Comparator.comparing(plan -> plan.planTime));
        return plans;
    }

    private void appendReminderPlans(List<PlanItem> plans, MedicineEntity medicine, ReminderEntity reminder, LocalDateTime start, LocalDateTime end) {
        LocalDate medicineStart = medicine.getCreatedAt().toLocalDate();
        int cycleDays = medicine.getCycleDays() == null || medicine.getCycleDays() < 1 ? 1 : medicine.getCycleDays();
        LocalDate medicineEnd = medicineStart.plusDays(cycleDays - 1L);
        LocalDate cursor = maxDate(start.toLocalDate(), medicineStart);
        LocalDate last = minDate(end.toLocalDate(), medicineEnd);
        if (cursor.isAfter(last)) {
            return;
        }
        List<String> remindTimes = JsonLists.fromJson(objectMapper, reminder.getRemindTimes());
        while (!cursor.isAfter(last)) {
            if (shouldUseDate(cursor, reminder.getRepeatType())) {
                for (String timeValue : remindTimes) {
                    LocalDateTime planTime = cursor.atTime(LocalTime.parse(timeValue, TIME_FORMAT));
                    if (!planTime.isBefore(start) && !planTime.isAfter(end)) {
                        plans.add(new PlanItem(medicine.getId(), medicine.getMedicineName(), planTime));
                    }
                }
            }
            cursor = cursor.plusDays(1);
        }
    }

    private boolean shouldUseDate(LocalDate date, String repeatType) {
        if (!"weekday".equals(repeatType)) {
            return true;
        }
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    private LocalDate maxDate(LocalDate left, LocalDate right) {
        return left.isAfter(right) ? left : right;
    }

    private LocalDate minDate(LocalDate left, LocalDate right) {
        return left.isBefore(right) ? left : right;
    }

    public static class PlanItem {
        public final Long medicineId;
        public final String medicineName;
        public final LocalDateTime planTime;

        public PlanItem(Long medicineId, String medicineName, LocalDateTime planTime) {
            this.medicineId = medicineId;
            this.medicineName = medicineName;
            this.planTime = planTime;
        }
    }
}
