package com.stock.forum.dto;

import javax.validation.constraints.NotBlank;
import java.util.List;

public final class RecordDtos {
    private RecordDtos() {
    }

    public static class AddRequest {
        @NotBlank
        public String userId;
        @NotBlank
        public String medicineId;
        @NotBlank
        public String planTime;
        @NotBlank
        public String actualTime;
    }

    public static class AddResponse {
        public String recordId;
    }

    public static class RecordItem {
        public String id;
        public String recordId;
        public String medicineId;
        public String medicineName;
        public String planTime;
        public String actualTime;
        public String status;
    }

    public static class StatResponse {
        public int total;
        public int completed;
        public int missed;
        public int rate;
        public int completionRate;
        public int missTimes;
        public int totalDays;
        public List<DailyStatItem> dailyStats;
        public List<MedicineStatItem> medicineStats;
    }

    public static class DailyStatItem {
        public String date;
        public String label;
        public int done;
        public int total;
        public int percent;
        public int missTimes;
    }

    public static class MedicineStatItem {
        public String name;
        public String medicineName;
        public int total;
        public int done;
        public int missed;
        public int missTimes;
        public int rate;
        public int completionRate;
    }
}
