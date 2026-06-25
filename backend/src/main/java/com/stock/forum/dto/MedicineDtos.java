package com.stock.forum.dto;

public final class MedicineDtos {
    private MedicineDtos() {
    }

    public static class MedicineItem {
        public String medicineId;
        public String medicineName;
        public Integer dailyTimes;
        public String dose;
        public String takeTime;
        public Integer cycle;
        public String createTime;
    }

    public static class DeleteResponse {
        public boolean success;
    }
}
