package com.stock.forum.controller;

import com.stock.forum.common.ApiResponse;
import com.stock.forum.dto.UserDtos;
import com.stock.forum.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/medicineBox/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<UserDtos.LoginResponse> login(@Valid @RequestBody UserDtos.LoginRequest request) {
        return ApiResponse.success("login success", userService.login(request.code));
    }

    @GetMapping("/info")
    public ApiResponse<UserDtos.UserInfoResponse> info(@RequestParam String userId) {
        return ApiResponse.success(userService.getInfo(userId));
    }
}
