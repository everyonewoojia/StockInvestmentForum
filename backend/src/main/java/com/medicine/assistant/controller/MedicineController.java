package com.medicine.assistant.controller;

import com.medicine.assistant.common.ApiResponse;
import com.medicine.assistant.dto.MedicineDtos;
import com.medicine.assistant.service.MedicineService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicineBox/medicine")
public class MedicineController {
    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping("/list")
    public ApiResponse<List<MedicineDtos.MedicineItem>> list(@RequestParam String userId) {
        return ApiResponse.success(medicineService.list(userId));
    }

    @DeleteMapping("/delete")
    public ApiResponse<MedicineDtos.DeleteResponse> delete(
            @RequestParam(required = false) String medicineId,
            @RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success("delete success", medicineService.delete(resolveId(medicineId, body, "medicineId")));
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
