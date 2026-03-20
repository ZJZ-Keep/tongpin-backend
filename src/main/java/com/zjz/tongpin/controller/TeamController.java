package com.zjz.tongpin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjz.tongpin.common.BaseResponse;
import com.zjz.tongpin.common.ErrorCode;
import com.zjz.tongpin.common.ResultUtils;
import com.zjz.tongpin.exception.BusinessException;
import com.zjz.tongpin.model.domain.Team;
import com.zjz.tongpin.model.domain.User;
import com.zjz.tongpin.model.dto.TeamQuery;
import com.zjz.tongpin.model.request.QuitTeamRequest;
import com.zjz.tongpin.model.request.TeamUpdateRequest;
import com.zjz.tongpin.model.request.UserJoinTeamRequest;
import com.zjz.tongpin.model.vo.TeamUserVo;
import com.zjz.tongpin.model.request.AddTeamRequest;
import com.zjz.tongpin.service.TeamService;
import com.zjz.tongpin.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 队伍接口
 *
 *   *   */
@RestController
@RequestMapping("/team")
@Slf4j
@Api(tags = "队伍接口")
public class TeamController {

    @Resource
    private UserService userService;
    @Resource
    private TeamService teamService;

    /**
     * 添加队伍
     */
    @PostMapping("/add")
    @ApiOperation("添加队伍")
    public BaseResponse<Long> addTeam(@RequestBody AddTeamRequest addTeamRequest, HttpServletRequest request){
        if (addTeamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.getUserLogin(request);
        Team team = new Team();
        BeanUtils.copyProperties(addTeamRequest,team);
        long teamId = teamService.addTeam(team, userLogin);
        if (teamId<0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"添加失败");
        }
        return ResultUtils.success(teamId);
    }


    /**
     * 修改队伍
     */
    @PostMapping("/update")
    @ApiOperation("修改队伍")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if (teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.getUserLogin(request);
        boolean update = teamService.updateTeam(teamUpdateRequest,userLogin);
        if (!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 查询队伍list
     */
    @GetMapping("/list")
    @ApiOperation("查询队伍list")
    public BaseResponse<List<TeamUserVo>> selectTeamList(TeamQuery teamQuery,HttpServletRequest request){
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        log.info("teamQuery:{}",teamQuery);
        List<TeamUserVo> list = teamService.listTeams(teamQuery,isAdmin);
        return ResultUtils.success(list);
    }

    /**
     * 查询队伍page
     */
    @GetMapping("/list/page")
    @ApiOperation("查询队伍page")
    public BaseResponse<Page<Team>> selectTeamPage(TeamQuery teamQuery){
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        log.info("teamQuery:{}",teamQuery);
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        QueryWrapper<Team> wrapper = new QueryWrapper<>(team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        Page<Team> resultPage = teamService.page(page, wrapper);
        if (resultPage==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"查询失败");
        }
        return ResultUtils.success(resultPage);
    }
    /**
     * 用户加入队伍
     */
    @PostMapping("/join")
    @ApiOperation("用户加入队伍")
    public BaseResponse<Boolean> UserJoinTeam(@RequestBody UserJoinTeamRequest userJoinTeamRequest,HttpServletRequest request){
        if (userJoinTeamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.getUserLogin(request);
        if (userLogin==null){
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        boolean joinTeam=teamService.joinTeam(userJoinTeamRequest,userLogin);
        return ResultUtils.success(joinTeam);
    }
    /**
     * 用户退出队伍
     */
    @PostMapping("/quit")
    @ApiOperation("用户退出队伍")
    public BaseResponse<Boolean> quitTeam(@RequestBody QuitTeamRequest quitTeamRequest,HttpServletRequest request){
        if (quitTeamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.getUserLogin(request);
        if (userLogin==null){
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        boolean quitTeam=teamService.quitTeam(quitTeamRequest,userLogin);
        return ResultUtils.success(quitTeam);
    }

    /**
     * 解散队伍
     */
    @PostMapping("/delete")
    @ApiOperation("解散队伍")
    public BaseResponse<Boolean> deleteTeam(@RequestBody Long teamId,HttpServletRequest request){
        if (teamId==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.getUserLogin(request);
        if (userLogin==null){
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        boolean deleteTeam=teamService.deleteTeam(teamId,userLogin);
        return ResultUtils.success(deleteTeam);
    }
}

