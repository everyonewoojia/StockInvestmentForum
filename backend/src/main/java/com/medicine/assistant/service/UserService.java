package com.medicine.assistant.service;

import com.medicine.assistant.auth.JwtService;
import com.medicine.assistant.common.ApiException;
import com.medicine.assistant.domain.UserEntity;
import com.medicine.assistant.dto.UserDtos;
import com.medicine.assistant.external.WechatClient;
import com.medicine.assistant.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final WechatClient wechatClient;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, WechatClient wechatClient, JwtService jwtService) {
        this.userRepository = userRepository;
        this.wechatClient = wechatClient;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserDtos.LoginResponse login(String code) {
        WechatClient.Session session = wechatClient.exchangeCode(code);
        UserEntity user = userRepository.findByOpenid(session.openid).orElseGet(UserEntity::new);
        user.setOpenid(session.openid);
        user.setSessionKey(session.sessionKey);
        if (user.getNickName() == null || user.getNickName().trim().isEmpty()) {
            user.setNickName("Wechat User");
        }
        if (user.getAvatar() == null) {
            user.setAvatar("");
        }
        user = userRepository.save(user);

        UserDtos.LoginResponse response = new UserDtos.LoginResponse();
        response.userId = String.valueOf(user.getId());
        response.nickName = user.getNickName();
        response.avatar = user.getAvatar();
        response.token = jwtService.createToken(user.getId());
        return response;
    }

    @Transactional(readOnly = true)
    public UserDtos.UserInfoResponse getInfo(String userId) {
        Long id = AuthGuard.requireSelf(userId);
        UserEntity user = userRepository.findById(id).orElseThrow(() -> ApiException.badRequest("User not found"));
        UserDtos.UserInfoResponse response = new UserDtos.UserInfoResponse();
        response.userId = String.valueOf(user.getId());
        response.nickName = user.getNickName();
        response.avatar = user.getAvatar();
        return response;
    }
}
