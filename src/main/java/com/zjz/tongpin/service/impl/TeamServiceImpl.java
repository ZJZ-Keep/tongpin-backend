package com.zjz.tongpin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjz.tongpin.common.ErrorCode;
import com.zjz.tongpin.exception.BusinessException;
import com.zjz.tongpin.model.domain.Team;
import com.zjz.tongpin.model.domain.User;
import com.zjz.tongpin.model.domain.UserTeam;
import com.zjz.tongpin.model.dto.TeamQuery;
import com.zjz.tongpin.model.request.QuitTeamRequest;
import com.zjz.tongpin.model.request.TeamUpdateRequest;
import com.zjz.tongpin.model.request.UserJoinTeamRequest;
import com.zjz.tongpin.model.vo.TeamUserVo;
import com.zjz.tongpin.model.enums.TeamStatusEnum;
import com.zjz.tongpin.model.vo.UserVo;
import com.zjz.tongpin.service.TeamService;
import com.zjz.tongpin.mapper.TeamMapper;
import com.zjz.tongpin.service.UserService;
import com.zjz.tongpin.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
* @author ZJZ-Keep
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2026-03-15 17:18:32
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;

    /**
     * 创建队伍
     * @param team
     * @param userLogin
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User userLogin) {
        //1. 请求参数是否为空？
        if (team == null){
            throw  new BusinessException(ErrorCode.NULL_ERROR);
        }
        //2.用户是否登录
        if (userLogin==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN,"用户未登录");
        }
        final Long userId = userLogin.getId();
        //  a. 队伍人数 > 1 且 <= 20
        int teamMaxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (teamMaxNum<1||teamMaxNum>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数异常");
        }
        //  b. 队伍标题 <= 20
        String teamName = team.getName();
        if (StringUtils.isBlank(teamName) ||teamName.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题异常");
        }
        //  c. 描述 <= 512
        String teamDescription = team.getDescription();
        if (StringUtils.isNotBlank(teamDescription)&&teamDescription.length()>512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述异常");
        }
        //  d. status 是否公开（int）不传默认为 0（公开）
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.teamStatusEnum(status);
        if (teamStatusEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态异常");
        }
        //  e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (Objects.equals(TeamStatusEnum.SECRET, teamStatusEnum)){
            if (StringUtils.isBlank(password)||password.length()>32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍密码异常");
            }
        }
        //  f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍超时时间异常");
        }
        //  g. 校验用户最多创建 5 个队伍
        //todo
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("userId",userId);
        long count = this.count(teamQueryWrapper);
        if (count>=5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍数量大于5");
        }
        //h. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean save = this.save(team);
        Long teamId = team.getId();
        if (!save||teamId==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"插入队伍表失败");
        }
        //i. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean saveUserTeam = userTeamService.save(userTeam);
        if (!saveUserTeam){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"插入队伍关系表失败");
        }
        return teamId;
    }

    /**
     * 获取列表（用户）
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        //1. 从请求参数中取出队伍名称等查询条件，如果存在则作为查询条件
        if (teamQuery!=null){
            Long id = teamQuery.getId();
            if (id!=null&&id>0){
                teamQueryWrapper.eq("id",id);
            }
            // 可以通过某个关键词同时对名称和描述查询
            String search = teamQuery.getSearch();
            if (StringUtils.isNotBlank(search)){
                teamQueryWrapper.and(tw ->tw.like("name",search).or().like("description",search));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)){
                teamQueryWrapper.like("name",name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)){
                teamQueryWrapper.like("description",description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum!=null&&maxNum>0){
                teamQueryWrapper.eq("maxNum",maxNum);
            }
            Long userId = teamQuery.getUserId();
            if (userId!=null&&userId>0){
                teamQueryWrapper.eq("userId",userId);
            }
            // 只有管理员才能查看加密还有非公开的房间
            Integer status = teamQuery.getStatus();
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.teamStatusEnum(status);
            if (teamStatusEnum==null){
                teamStatusEnum=TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin&&!teamStatusEnum.equals(TeamStatusEnum.PUBLIC)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            teamQueryWrapper.eq("status",teamStatusEnum.getStatus());
        }
        //2. 不展示已过期的队伍（根据过期时间筛选）
        teamQueryWrapper.and(tw->tw.gt("expireTime",new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(teamQueryWrapper);
        if (teamList==null){
            return new ArrayList<>();
        }

        //3. 关联查询已加入队伍的用户信息(创建人)
        ArrayList<TeamUserVo> teamUserVoArrayList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId==null){
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team,teamUserVo);
            if (user!=null){
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user,userVo);
                teamUserVo.setCreateUserVo(userVo);
            }
            teamUserVoArrayList.add(teamUserVo);
        }
        //todo 关联查询已加入队伍的用户信息（可能会很耗费性能，建议大家用自己写 SQL 的方式实现）
        return teamUserVoArrayList;
    }

    /**
     * 更新队伍信息
     * @param teamUpdateRequest
     * @param userLogin
     * @return
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User userLogin) {
        //1. 判断请求参数是否为空
        if (teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 查询队伍是否存在
        Long id = teamUpdateRequest.getId();
        if (id==null||id<=0){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam==null){
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        //3. 只有管理员或者队伍的创建者可以修改
        if (!Objects.equals(userLogin.getId(), oldTeam.getUserId()) &&!userService.isAdmin(userLogin)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        //4. 如果用户传入的新值和老值一致，就不用 update 了（可自行实现，降低数据库使用次数）
        //5. 如果队伍状态改为加密，必须要有密码
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.teamStatusEnum(teamUpdateRequest.getStatus());
        if (teamStatusEnum==TeamStatusEnum.SECRET){
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"改密码错误");
            }
        }
        //6. 更新成功
        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,team);

        return this.updateById(team);
    }

    /**
     * 加入队伍
     * @param userJoinTeamRequest
     * @param userLogin
     * @return
     */
    @Override
    public boolean joinTeam(UserJoinTeamRequest userJoinTeamRequest, User userLogin) {
        if (userJoinTeamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = userJoinTeamRequest.getTeamId();
        if (teamId==null||teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //队伍不存在
        Team team = this.getById(teamId);
        if (team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        //4. 禁止加入私有的队伍
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.teamStatusEnum(team.getStatus());
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍私有");
        }
        //未过期的队伍
        Date expireTime = team.getExpireTime();
        if (expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍已过期");
        }
        //5. 如果加入的队伍是加密的，必须密码匹配才可以
        String password = userJoinTeamRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        //1. 用户最多加入 5 个队伍
        Long userId = userLogin.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if(count>5){
            throw new BusinessException(ErrorCode.NULL_ERROR,"用户错误");
        }
        //2. 队伍必须存在，、
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        count = userTeamService.count(userTeamQueryWrapper);
        //只能加入未满
        if (team.getMaxNum()<=count){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍已满");
        }
        // 不能重复加入已加入的队伍（幂等性）
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        userTeamQueryWrapper.eq("userId",userId);
        count = userTeamService.count(userTeamQueryWrapper);
        if (count>0){
            throw new BusinessException(ErrorCode.NULL_ERROR,"不能重复加入已加入的队伍");
        }
        //6. 新增队伍 - 用户关联信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);

    }
    /**
     * 用户退出队伍
     * @param quitTeamRequest
     * @param userLogin
     * @return
     */
    @Override
    public boolean quitTeam(QuitTeamRequest quitTeamRequest, User userLogin) {
        //1. 校验请求参数
        if (quitTeamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 校验队伍是否存在
        Long teamId = quitTeamRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //3. 校验我是否已加入队伍
        Long userId = userLogin.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        userTeamQueryWrapper.eq("userId",userId);
        long countRelax = userTeamService.count(userTeamQueryWrapper);
        if (countRelax<1){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"未加入队伍");
        }
        //4. 如果队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId",teamId);
        long count = userTeamService.count(queryWrapper);
        if (count==1){
            //  a. 只剩一人，队伍解散
            this.removeById(teamId);
        }else {
            //  b. 还有其他人
            //    ⅰ. 如果是队长退出队伍，权限转移给第二早加入的用户 —— 先来后到只用取 id 最小的 2 条数据
            if (userId.equals(team.getUserId())){
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("teamId",teamId);
                queryWrapper.last("order by id asc limit 2");
                List<UserTeam> list = userTeamService.list(queryWrapper);
                if (CollectionUtils.isEmpty(list)||list.size()<=1){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam newUserTeam = list.get(1);
                Long newUserId = newUserTeam.getUserId();
                team.setUserId(newUserId);
                boolean update = this.updateById(team);
                if (!update){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队长失败");
                }
            }
        }
        return userTeamService.remove(userTeamQueryWrapper);
    }
}




