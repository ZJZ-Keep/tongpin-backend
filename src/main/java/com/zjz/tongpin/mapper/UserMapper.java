package com.zjz.tongpin.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjz.tongpin.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.core.Constants;

import java.util.List;

/**
 * 用户 Mapper
 *
 *   *   */
public interface UserMapper extends BaseMapper<User> {

    List<User> selectListByCustomSql(@Param("ew") QueryWrapper<User> wrapper);
}



