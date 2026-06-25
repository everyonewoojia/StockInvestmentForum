package com.stock.forum.controller;

import com.stock.forum.common.ApiResponse;
import com.stock.forum.dto.RecordDtos;
import com.stock.forum.service.RecordService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/medicineBox/record")
public class RecordController {
    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping("/add")
    public ApiResponse<RecordDtos.AddResponse> add(@Valid @RequestBody RecordDtos.AddRequest request) {
        return ApiResponse.success("add record success", recordService.add(request));
    }

    @GetMapping("/list")
    public ApiResponse<List<RecordDtos.RecordItem>> list(
            @RequestParam String userId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        return ApiResponse.success(recordService.list(userId, startTime, endTime));
    }

    @GetMapping("/stat")
    public ApiResponse<RecordDtos.StatResponse> stat(
            @RequestParam String userId,
            @RequestParam String type,
            @RequestParam String date) {
        return ApiResponse.success(recordService.stat(userId, type, date));
    }
}
