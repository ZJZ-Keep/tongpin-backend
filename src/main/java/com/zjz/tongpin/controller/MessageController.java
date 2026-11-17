package com.zjz.tongpin.controller;

import com.zjz.tongpin.common.BaseResponse;
import com.zjz.tongpin.common.ErrorCode;
import com.zjz.tongpin.common.ResultUtils;
import com.zjz.tongpin.exception.BusinessException;
import com.zjz.tongpin.model.domain.Message;
import com.zjz.tongpin.model.domain.User;
import com.zjz.tongpin.model.vo.MessageVo;
import com.zjz.tongpin.service.MessageService;
import com.zjz.tongpin.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 聊天消息接口
 */
@RestController
@RequestMapping("/message")
@Api(tags = "聊天消息接口")
public class MessageController {

    @Resource
    private MessageService messageService;
    @Resource
    private UserService userService;

    /**
     * 获取与某人的历史消息
     */
    @GetMapping("/history")
    @ApiOperation("获取与某人的历史消息")
    public BaseResponse<List<Message>> getHistory(@RequestParam Long targetId,
                                                  @RequestParam(defaultValue = "1") long pageNum,
                                                  @RequestParam(defaultValue = "100") long pageSize,
                                                  HttpServletRequest request) {
        User loginUser = userService.getUserLogin(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (targetId == null || targetId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(messageService.getHistory(loginUser.getId(), targetId, pageNum, pageSize));
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    @ApiOperation("获取会话列表")
    public BaseResponse<List<MessageVo>> getConversations(HttpServletRequest request) {
        User loginUser = userService.getUserLogin(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return ResultUtils.success(messageService.getConversations(loginUser.getId()));
    }

    /**
     * 统计未读消息数
     */
    @GetMapping("/unread/count")
    @ApiOperation("统计未读消息数")
    public BaseResponse<Long> countUnread(HttpServletRequest request) {
        User loginUser = userService.getUserLogin(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return ResultUtils.success(messageService.countUnread(loginUser.getId()));
    }

    /**
     * 标记与某人的消息为已读
     */
    @PostMapping("/read")
    @ApiOperation("标记与某人的消息为已读")
    public BaseResponse<Boolean> markRead(@RequestBody Long targetId, HttpServletRequest request) {
        User loginUser = userService.getUserLogin(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (targetId == null || targetId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(messageService.markRead(loginUser.getId(), targetId));
    }
}
