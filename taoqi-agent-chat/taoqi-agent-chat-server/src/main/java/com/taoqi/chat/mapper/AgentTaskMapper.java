package com.taoqi.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.taoqi.chat.api.entity.AgentTaskEntity;

/**
 * AI 任务的数据访问接口：CRUD 与分页由 MyBatis-Plus 提供。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@Mapper
public interface AgentTaskMapper extends BaseMapper<AgentTaskEntity> {
}