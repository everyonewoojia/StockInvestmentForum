package com.stock.forum.controller;

import com.stock.forum.common.ApiResponse;
import com.stock.forum.dto.ReminderDtos;
import com.stock.forum.service.ReminderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/medicineBox/reminder")
public class ReminderController {
    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping("/add")
    public ApiResponse<ReminderDtos.AddResponse> add(@Valid @RequestBody ReminderDtos.AddRequest request) {
        return ApiResponse.success("add reminder success", reminderService.add(request));
    }

    @GetMapping("/list")
    public ApiResponse<List<ReminderDtos.ReminderItem>> list(@RequestParam String userId) {
        return ApiResponse.success(reminderService.list(userId));
    }

    @PostMapping("/update")
    public ApiResponse<ReminderDtos.BooleanResponse> update(@Valid @RequestBody ReminderDtos.UpdateRequest request) {
        return ApiResponse.success("update reminder success", reminderService.update(request));
    }

    @DeleteMapping("/delete")
    public ApiResponse<ReminderDtos.BooleanResponse> delete(
            @RequestParam(required = false) String reminderId,
            @RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success("delete reminder success", reminderService.delete(resolveId(reminderId, body, "reminderId")));
    }

    private String resolveId(String queryValue, Map<String, Object> body, String key) {
        if (queryValue != null && !queryValue.trim().isEmpty()) {
            return queryValue;
        }
        if (body != null && body.get(key) != null) {
            return String.valueOf(body.get(key));
        }
        return "";
    }
}
