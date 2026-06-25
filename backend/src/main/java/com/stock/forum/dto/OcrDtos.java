package com.stock.forum.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public final class OcrDtos {
    private OcrDtos() {
    }

    public static class UploadResponse {
        public String imageUrl;
    }

    public static class RecognizeRequest {
        @NotBlank
        public String userId;
        @NotBlank
        public String imageUrl;
    }

    public static class MedicineInfoResponse {
        public String medicineName = "";
        public Integer dailyTimes;
        public String dose = "";
        public String takeTime = "饭后";
        public Integer cycle;
    }

    public static class MedicineSaveRequest {
        @NotBlank
        public String userId;
        @NotBlank
        public String medicineName;
        @NotNull
        public Integer dailyTimes;
        @NotBlank
        public String dose;
        @NotBlank
        public String takeTime;
        @NotNull
        public Integer cycle;
    }

    public static class MedicineSaveResponse {
        public String medicineId;
    }
}
