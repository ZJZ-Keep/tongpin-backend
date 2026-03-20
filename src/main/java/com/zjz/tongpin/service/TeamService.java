package com.zjz.tongpin.service;

import com.zjz.tongpin.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjz.tongpin.model.domain.User;
import com.zjz.tongpin.model.dto.TeamQuery;
import com.zjz.tongpin.model.request.QuitTeamRequest;
import com.zjz.tongpin.model.request.TeamUpdateRequest;
import com.zjz.tongpin.model.request.UserJoinTeamRequest;
import com.zjz.tongpin.model.vo.TeamUserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author ZJZ-Keep
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2026-03-15 17:18:33
*/
public interface TeamService extends IService<Team> {
    /**
     * 添加队伍
     * @param team
     * @param userLogin
     * @return
     */
    public long addTeam(Team team, User userLogin);

    /**
     * 查询队伍list
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 修改队伍
     * @param teamUpdateRequest
     * @param userLogin
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User userLogin);

    /**
     * 加入队伍
     * @param userJoinTeamRequest
     * @param userLogin
     * @return
     */
    boolean joinTeam(UserJoinTeamRequest userJoinTeamRequest, User userLogin);

    /**
     * 用户退出队伍
     * @param quitTeamRequest
     * @param userLogin
     * @return
     */
    boolean quitTeam(QuitTeamRequest quitTeamRequest, User userLogin);
    /**
     * 解散队伍
     * @param teamId
     * @param userLogin
     * @return
     */
    boolean deleteTeam(Long teamId, User userLogin);

}
