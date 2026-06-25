package com.stock.forum.controller;

import com.stock.forum.common.ApiResponse;
import com.stock.forum.common.PageResponse;
import com.stock.forum.dto.ForumDtos;
import com.stock.forum.service.ForumService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
public class ForumController {
    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @PostMapping("/auth/register")
    public ApiResponse<ForumDtos.AuthResponse> register(@RequestBody ForumDtos.RegisterRequest request) {
        return ApiResponse.success("注册成功", forumService.register(request));
    }

    @PostMapping("/auth/login")
    public ApiResponse<ForumDtos.AuthResponse> login(@RequestBody ForumDtos.LoginRequest request) {
        return ApiResponse.success("登录成功", forumService.login(request));
    }

    @GetMapping("/auth/me")
    public ApiResponse<Map<String, Object>> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.success(forumService.me(authorization));
    }

    @PutMapping("/users/me")
    public ApiResponse<Map<String, Object>> updateProfile(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                          @RequestBody ForumDtos.ProfileRequest request) {
        return ApiResponse.success("资料已更新", forumService.updateProfile(authorization, request));
    }

    @PostMapping("/users/me/verifications")
    public ApiResponse<Map<String, Object>> submitVerification(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                               @RequestBody ForumDtos.VerificationRequest request) {
        return ApiResponse.success("认证申请已提交", forumService.submitVerification(authorization, request));
    }

    @PostMapping("/users/me/risk-assessment")
    public ApiResponse<Map<String, Object>> completeRiskAssessment(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                                   @RequestBody ForumDtos.RiskAssessmentRequest request) {
        return ApiResponse.success("适当性评估已完成", forumService.completeRiskAssessment(authorization, request));
    }

    @GetMapping("/boards")
    public ApiResponse<List<Map<String, Object>>> listBoards(@RequestParam(value = "includeDisabled", defaultValue = "false") boolean includeDisabled) {
        return ApiResponse.success(forumService.listBoards(includeDisabled));
    }

    @GetMapping("/posts")
    public ApiResponse<PageResponse<Map<String, Object>>> listPosts(@RequestParam(value = "boardId", required = false) Long boardId,
                                                                    @RequestParam(value = "keyword", required = false) String keyword,
                                                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                                                    @RequestParam(value = "size", defaultValue = "10") int size) {
        return ApiResponse.success(forumService.listPublishedPosts(boardId, keyword, page, size));
    }

    @GetMapping("/posts/mine")
    public ApiResponse<PageResponse<Map<String, Object>>> myPosts(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                                                  @RequestParam(value = "size", defaultValue = "10") int size) {
        return ApiResponse.success(forumService.listMyPosts(authorization, page, size));
    }

    @PostMapping("/posts")
    public ApiResponse<Map<String, Object>> createPost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                       @RequestBody ForumDtos.PostRequest request) {
        return ApiResponse.success("帖子已进入审核队列", forumService.createPost(authorization, request));
    }

    @GetMapping("/posts/{postId}")
    public ApiResponse<Map<String, Object>> getPost(@PathVariable Long postId,
                                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.success(forumService.getPost(postId, authorization));
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<Map<String, Object>>> listComments(@PathVariable Long postId) {
        return ApiResponse.success(forumService.listComments(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<Map<String, Object>> createComment(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                          @PathVariable Long postId,
                                                          @RequestBody ForumDtos.CommentRequest request) {
        return ApiResponse.success("评论成功", forumService.createComment(authorization, postId, request));
    }

    @PostMapping("/posts/{postId}/interactions")
    public ApiResponse<Map<String, Object>> interact(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                     @PathVariable Long postId,
                                                     @RequestBody ForumDtos.InteractionRequest request) {
        return ApiResponse.success(forumService.interact(authorization, postId, request));
    }

    @PostMapping("/reports")
    public ApiResponse<Map<String, Object>> report(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                   @RequestBody ForumDtos.ReportRequest request) {
        return ApiResponse.success("举报已提交", forumService.report(authorization, request));
    }

    @PostMapping("/social/follow/{targetUserId}")
    public ApiResponse<Map<String, Object>> follow(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                   @PathVariable Long targetUserId,
                                                   @RequestParam(value = "starred", defaultValue = "false") boolean starred) {
        return ApiResponse.success(forumService.follow(authorization, targetUserId, true, starred));
    }

    @DeleteMapping("/social/follow/{targetUserId}")
    public ApiResponse<Map<String, Object>> unfollow(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                     @PathVariable Long targetUserId) {
        return ApiResponse.success(forumService.follow(authorization, targetUserId, false, false));
    }

    @GetMapping("/social/following-feed")
    public ApiResponse<PageResponse<Map<String, Object>>> followingFeed(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                                        @RequestParam(value = "page", defaultValue = "1") int page,
                                                                        @RequestParam(value = "size", defaultValue = "10") int size) {
        return ApiResponse.success(forumService.followingFeed(authorization, page, size));
    }

    @GetMapping("/search")
    public ApiResponse<Map<String, Object>> search(@RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "page", defaultValue = "1") int page,
                                                   @RequestParam(value = "size", defaultValue = "10") int size) {
        return ApiResponse.success(forumService.search(keyword, page, size));
    }

    @GetMapping("/search/suggest")
    public ApiResponse<List<Map<String, Object>>> suggest(@RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.success(forumService.suggest(keyword));
    }

    @GetMapping("/groups")
    public ApiResponse<List<Map<String, Object>>> listGroups() {
        return ApiResponse.success(forumService.listGroups());
    }

    @PostMapping("/groups")
    public ApiResponse<Map<String, Object>> createGroup(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                        @RequestBody ForumDtos.GroupRequest request) {
        return ApiResponse.success("群组已创建", forumService.createGroup(authorization, request));
    }

    @PostMapping("/groups/{groupId}/join")
    public ApiResponse<Map<String, Object>> joinGroup(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                      @PathVariable Long groupId) {
        return ApiResponse.success(forumService.joinGroup(authorization, groupId));
    }

    @PostMapping("/messages")
    public ApiResponse<Map<String, Object>> sendMessage(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                        @RequestBody ForumDtos.MessageRequest request) {
        return ApiResponse.success("私信已发送", forumService.sendMessage(authorization, request));
    }

    @GetMapping("/messages/{peerId}")
    public ApiResponse<List<Map<String, Object>>> listMessages(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                               @PathVariable Long peerId) {
        return ApiResponse.success(forumService.listMessages(authorization, peerId));
    }

    @GetMapping("/admin/dashboard")
    public ApiResponse<Map<String, Object>> adminDashboard(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.success(forumService.adminDashboard(authorization));
    }

    @GetMapping("/admin/review/posts")
    public ApiResponse<PageResponse<Map<String, Object>>> adminReviewPosts(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                                           @RequestParam(value = "status", defaultValue = "PENDING_REVIEW") String status,
                                                                           @RequestParam(value = "page", defaultValue = "1") int page,
                                                                           @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.success(forumService.adminReviewPosts(authorization, status, page, size));
    }

    @PostMapping("/admin/review/posts/{postId}")
    public ApiResponse<Map<String, Object>> reviewPost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                       @PathVariable Long postId,
                                                       @RequestBody ForumDtos.ReviewRequest request) {
        return ApiResponse.success("审核完成", forumService.reviewPost(authorization, postId, request));
    }

    @GetMapping("/admin/reports")
    public ApiResponse<PageResponse<Map<String, Object>>> listReports(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                                      @RequestParam(value = "status", required = false) String status,
                                                                      @RequestParam(value = "page", defaultValue = "1") int page,
                                                                      @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.success(forumService.listReports(authorization, status, page, size));
    }

    @PostMapping("/admin/reports/{reportId}/resolve")
    public ApiResponse<Map<String, Object>> resolveReport(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                          @PathVariable Long reportId,
                                                          @RequestBody ForumDtos.ReviewRequest request) {
        return ApiResponse.success("举报已处理", forumService.resolveReport(authorization, reportId, request));
    }

    @GetMapping("/admin/users")
    public ApiResponse<PageResponse<Map<String, Object>>> listUsers(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                                    @RequestParam(value = "keyword", required = false) String keyword,
                                                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                                                    @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.success(forumService.listUsers(authorization, keyword, page, size));
    }

    @PostMapping("/admin/users/{userId}/violation")
    public ApiResponse<Map<String, Object>> applyViolation(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                           @PathVariable Long userId,
                                                           @RequestBody ForumDtos.ViolationRequest request) {
        return ApiResponse.success("处理完成", forumService.applyViolation(authorization, userId, request));
    }

    @PostMapping("/admin/boards")
    public ApiResponse<Map<String, Object>> createBoard(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                        @RequestBody ForumDtos.BoardRequest request) {
        return ApiResponse.success("板块已创建", forumService.createBoard(authorization, request));
    }

    @PutMapping("/admin/boards/{boardId}")
    public ApiResponse<Map<String, Object>> updateBoard(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                        @PathVariable Long boardId,
                                                        @RequestBody ForumDtos.BoardRequest request) {
        return ApiResponse.success("板块已更新", forumService.updateBoard(authorization, boardId, request));
    }

    @DeleteMapping("/admin/boards/{boardId}")
    public ApiResponse<Map<String, Object>> deleteBoard(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                        @PathVariable Long boardId) {
        return ApiResponse.success("板块已停用", forumService.deleteBoard(authorization, boardId));
    }

    @GetMapping("/admin/sensitive-words")
    public ApiResponse<List<Map<String, Object>>> listSensitiveWords(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.success(forumService.listSensitiveWords(authorization));
    }

    @PostMapping("/admin/sensitive-words")
    public ApiResponse<Map<String, Object>> createSensitiveWord(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                                @RequestBody ForumDtos.SensitiveWordRequest request) {
        return ApiResponse.success("敏感词已添加", forumService.createSensitiveWord(authorization, request));
    }

    @DeleteMapping("/admin/sensitive-words/{wordId}")
    public ApiResponse<Map<String, Object>> deleteSensitiveWord(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                                @PathVariable Long wordId) {
        return ApiResponse.success("敏感词已删除", forumService.deleteSensitiveWord(authorization, wordId));
    }
}
