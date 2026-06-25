package com.stock.forum.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public final class ReminderDtos {
    private ReminderDtos() {
    }

    public static class AddRequest {
        @NotBlank
        public String userId;
        @NotBlank
        public String medicineId;
        @NotEmpty
        public List<String> remindTimes;
        @NotBlank
        public String repeatType;
        public String status;
    }

    public static class AddResponse {
        public String reminderId;
    }

    public static class UpdateRequest {
        @NotBlank
        public String reminderId;
        public List<String> remindTimes;
        public String repeatType;
        public String status;
    }

    public static class ReminderItem {
        public String reminderId;
        public String medicineId;
        public String medicineName;
        public List<String> remindTimes;
        public String repeatType;
        public String status;
        public String createTime;
    }

    public static class BooleanResponse {
        public boolean success;
    }
}
