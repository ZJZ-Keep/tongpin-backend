package com.zjz.tongpin.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjz.tongpin.model.domain.Team;
import com.zjz.tongpin.model.domain.UserTeam;
import com.zjz.tongpin.service.TeamService;
import com.zjz.tongpin.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 过期队伍清理定时任务：
 * 队伍过期后逻辑删除队伍（isDelete = 1），并移除对应的加入关系，
 * 使其不再展示、也不再参与「加入/创建 5 个队伍」的数量限制。
 */
@Component
@Slf4j
public class TeamExpireJob {

    @Resource
    private TeamService teamService;
    @Resource
    private UserTeamService userTeamService;

    /**
     * 每分钟执行一次，清理已过期队伍
     */
    @Scheduled(cron = "0 * * * * ?")
    public void cleanExpiredTeams() {
        Date now = new Date();
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        wrapper.lt("expireTime", now);
        List<Team> expiredTeams = teamService.list(wrapper);
        if (expiredTeams == null || expiredTeams.isEmpty()) {
            return;
        }
        for (Team team : expiredTeams) {
            // 移除加入关系（逻辑删除）
            QueryWrapper<UserTeam> relationWrapper = new QueryWrapper<>();
            relationWrapper.eq("teamId", team.getId());
            userTeamService.remove(relationWrapper);
            // 逻辑删除队伍
            teamService.removeById(team.getId());
        }
        log.info("已清理过期队伍 {} 个", expiredTeams.size());
    }
}
