package com.zjz.tongpin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjz.tongpin.model.domain.UserTeam;
import com.zjz.tongpin.service.UserTeamService;
import com.zjz.tongpin.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author ZJZ-Keep
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2026-03-15 17:20:03
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




