package com.zjz.tongpin.service;

import com.zjz.tongpin.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjz.tongpin.model.domain.User;
import com.zjz.tongpin.model.dto.TeamQuery;
import com.zjz.tongpin.model.request.TeamUpdateRequest;
import com.zjz.tongpin.model.vo.TeamUserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 23021
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2026-03-15 17:18:33
*/
public interface TeamService extends IService<Team> {
    public long addTeam(Team team, User userLogin);

    List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User userLogin);
}
