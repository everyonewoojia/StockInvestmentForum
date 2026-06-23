package com.medicine.assistant.service;

import com.medicine.assistant.common.ApiException;
import com.medicine.assistant.domain.MedicineEntity;
import com.medicine.assistant.domain.RecordEntity;
import com.medicine.assistant.dto.RecordDtos;
import com.medicine.assistant.repository.MedicineRepository;
import com.medicine.assistant.repository.RecordRepository;
import com.medicine.assistant.util.DateTimes;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecordService {
    private static final long MISSED_GRACE_MINUTES = 5L;
    private final RecordRepository recordRepository;
    private final MedicineRepository medicineRepository;
    private final RecordPlanService planService;

    public RecordService(RecordRepository recordRepository, MedicineRepository medicineRepository, RecordPlanService planService) {
        this.recordRepository = recordRepository;
        this.medicineRepository = medicineRepository;
        this.planService = planService;
    }

    @Transactional
    public RecordDtos.AddResponse add(RecordDtos.AddRequest request) {
        Long userId = AuthGuard.requireSelf(request.userId);
        Long medicineId = AuthGuard.parseId(request.medicineId, "Invalid medicineId");
        medicineRepository.findByIdAndUserIdAndDeletedFalse(medicineId, userId)
                .orElseThrow(() -> ApiException.badRequest("Medicine not found"));
        LocalDateTime planTime = parseDateTime(request.planTime, "Invalid planTime");
        LocalDateTime actualTime = parseDateTime(request.actualTime, "Invalid actualTime");

        RecordEntity record = recordRepository.findByUserIdAndMedicineIdAndPlanTime(userId, medicineId, planTime)
                .orElseGet(RecordEntity::new);
        record.setUserId(userId);
        record.setMedicineId(medicineId);
        record.setPlanTime(planTime);
        record.setActualTime(actualTime);
        record.setStatus("taken");
        try {
            record = recordRepository.save(record);
        } catch (DataIntegrityViolationException ex) {
            record = recordRepository.findByUserIdAndMedicineIdAndPlanTime(userId, medicineId, planTime)
                    .orElseThrow(() -> ApiException.serverError("Failed to save record"));
            record.setActualTime(actualTime);
            record.setStatus("taken");
            record = recordRepository.save(record);
        }

        RecordDtos.AddResponse response = new RecordDtos.AddResponse();
        response.recordId = String.valueOf(record.getId());
        return response;
    }

    @Transactional(readOnly = true)
    public List<RecordDtos.RecordItem> list(String userIdValue, String startTimeValue, String endTimeValue) {
        Long userId = AuthGuard.requireSelf(userIdValue);
        LocalDateTime start = parseDateTime(startTimeValue, "Invalid startTime");
        LocalDateTime end = parseDateTime(endTimeValue, "Invalid endTime");
        if (end.isBefore(start)) {
            throw ApiException.badRequest("endTime must be after startTime");
        }
        LocalDateTime now = LocalDateTime.now();
        List<RecordPlanService.PlanItem> plans = planService.buildPlans(userId, start, end);
        List<RecordEntity> records = recordRepository.findByUserIdAndPlanTimeBetweenOrderByPlanTimeAsc(userId, start, end);
        Map<String, RecordEntity> recordsByPlan = recordsByPlan(records);
        Map<String, RecordDtos.RecordItem> itemsByPlan = new LinkedHashMap<String, RecordDtos.RecordItem>();

        for (RecordPlanService.PlanItem plan : plans) {
            String key = key(plan.medicineId, plan.planTime);
            RecordEntity record = recordsByPlan.get(key);
            itemsByPlan.put(key, toRecordItem(plan, record, now));
        }
        for (RecordEntity record : records) {
            String key = key(record.getMedicineId(), record.getPlanTime());
            if (!itemsByPlan.containsKey(key)) {
                itemsByPlan.put(key, toActualOnlyItem(userId, record));
            }
        }

        List<RecordDtos.RecordItem> items = new ArrayList<RecordDtos.RecordItem>(itemsByPlan.values());
        items.sort(Comparator.comparing(item -> item.planTime));
        return items;
    }

    @Transactional(readOnly = true)
    public RecordDtos.StatResponse stat(String userIdValue, String type, String date) {
        Long userId = AuthGuard.requireSelf(userIdValue);
        if (!"week".equals(type) && !"month".equals(type)) {
            throw ApiException.badRequest("type must be week or month");
        }
        DateTimes.Range range = DateTimes.rangeForStat(type, date);
        LocalDateTime now = LocalDateTime.now();
        List<RecordPlanService.PlanItem> plans = planService.buildPlans(userId, range.start, range.end);
        Map<String, RecordEntity> records = recordsByPlan(recordRepository.findByUserIdAndPlanTimeBetweenOrderByPlanTimeAsc(userId, range.start, range.end));

        int completed = 0;
        int missed = 0;
        Map<LocalDate, DailyCounter> daily = new LinkedHashMap<LocalDate, DailyCounter>();
        for (LocalDate day : DateTimes.daysBetween(range.start.toLocalDate(), range.end.toLocalDate())) {
            daily.put(day, new DailyCounter(day));
        }
        Map<Long, MedicineCounter> medicines = new LinkedHashMap<Long, MedicineCounter>();

        for (RecordPlanService.PlanItem plan : plans) {
            boolean taken = records.containsKey(key(plan.medicineId, plan.planTime));
            boolean planMissed = !taken && isMissed(plan.planTime, now);
            if (taken) {
                completed++;
            }
            if (planMissed) {
                missed++;
            }
            DailyCounter day = daily.get(plan.planTime.toLocalDate());
            if (day != null) {
                day.total++;
                if (taken) day.done++;
                if (planMissed) day.missed++;
            }
            MedicineCounter medicine = medicines.get(plan.medicineId);
            if (medicine == null) {
                medicine = new MedicineCounter(plan.medicineName);
                medicines.put(plan.medicineId, medicine);
            }
            medicine.total++;
            if (taken) medicine.done++;
            if (planMissed) medicine.missed++;
        }

        RecordDtos.StatResponse response = new RecordDtos.StatResponse();
        response.total = plans.size();
        response.completed = completed;
        response.missed = missed;
        response.rate = percent(completed, response.total);
        response.completionRate = response.rate;
        response.missTimes = missed;
        response.totalDays = daily.size();
        response.dailyStats = new ArrayList<RecordDtos.DailyStatItem>();
        for (DailyCounter counter : daily.values()) {
            response.dailyStats.add(counter.toDto("week".equals(type)));
        }
        response.medicineStats = new ArrayList<RecordDtos.MedicineStatItem>();
        for (MedicineCounter counter : medicines.values()) {
            response.medicineStats.add(counter.toDto());
        }
        return response;
    }

    private RecordDtos.RecordItem toRecordItem(RecordPlanService.PlanItem plan, RecordEntity record, LocalDateTime now) {
        RecordDtos.RecordItem item = new RecordDtos.RecordItem();
        item.medicineId = String.valueOf(plan.medicineId);
        item.medicineName = plan.medicineName;
        item.planTime = DateTimes.format(plan.planTime);
        if (record != null) {
            item.id = String.valueOf(record.getId());
            item.recordId = String.valueOf(record.getId());
            item.actualTime = DateTimes.format(record.getActualTime());
            item.status = "taken";
        } else {
            item.id = "plan-" + plan.medicineId + "-" + DateTimes.format(plan.planTime);
            item.recordId = "";
            item.actualTime = "";
            item.status = isMissed(plan.planTime, now) ? "missed" : "pending";
        }
        return item;
    }

    private RecordDtos.RecordItem toActualOnlyItem(Long userId, RecordEntity record) {
        MedicineEntity medicine = medicineRepository.findByIdAndUserId(record.getMedicineId(), userId).orElse(null);
        RecordDtos.RecordItem item = new RecordDtos.RecordItem();
        item.id = String.valueOf(record.getId());
        item.recordId = String.valueOf(record.getId());
        item.medicineId = String.valueOf(record.getMedicineId());
        item.medicineName = medicine == null ? "" : medicine.getMedicineName();
        item.planTime = DateTimes.format(record.getPlanTime());
        item.actualTime = DateTimes.format(record.getActualTime());
        item.status = "taken";
        return item;
    }

    private Map<String, RecordEntity> recordsByPlan(List<RecordEntity> records) {
        Map<String, RecordEntity> map = new HashMap<String, RecordEntity>();
        for (RecordEntity record : records) {
            map.put(key(record.getMedicineId(), record.getPlanTime()), record);
        }
        return map;
    }

    private String key(Long medicineId, LocalDateTime planTime) {
        return medicineId + "|" + DateTimes.format(planTime);
    }

    private LocalDateTime parseDateTime(String value, String message) {
        try {
            return DateTimes.parseDateTime(value);
        } catch (Exception ex) {
            throw ApiException.badRequest(message);
        }
    }

    private static int percent(int done, int total) {
        return total <= 0 ? 0 : (int) Math.round(done * 100.0 / total);
    }

    private boolean isMissed(LocalDateTime planTime, LocalDateTime now) {
        return planTime.plusMinutes(MISSED_GRACE_MINUTES).isBefore(now);
    }

    private static class DailyCounter {
        private static final DateTimeFormatter LABEL_WEEK = DateTimeFormatter.ofPattern("MM-dd");
        private static final DateTimeFormatter LABEL_MONTH = DateTimeFormatter.ofPattern("dd");
        private final LocalDate date;
        private int total;
        private int done;
        private int missed;

        private DailyCounter(LocalDate date) {
            this.date = date;
        }

        private RecordDtos.DailyStatItem toDto(boolean week) {
            RecordDtos.DailyStatItem item = new RecordDtos.DailyStatItem();
            item.date = date.toString();
            item.label = date.format(week ? LABEL_WEEK : LABEL_MONTH);
            item.done = done;
            item.total = total;
            item.percent = percent(done, total);
            item.missTimes = missed;
            return item;
        }
    }

    private static class MedicineCounter {
        private final String name;
        private int total;
        private int done;
        private int missed;

        private MedicineCounter(String name) {
            this.name = name;
        }

        private RecordDtos.MedicineStatItem toDto() {
            RecordDtos.MedicineStatItem item = new RecordDtos.MedicineStatItem();
            item.name = name;
            item.medicineName = name;
            item.total = total;
            item.done = done;
            item.missed = missed;
            item.missTimes = missed;
            item.rate = percent(done, total);
            item.completionRate = item.rate;
            return item;
        }
    }
}
