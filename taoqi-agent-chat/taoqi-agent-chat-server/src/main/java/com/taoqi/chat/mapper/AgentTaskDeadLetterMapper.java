package com.taoqi.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.taoqi.chat.api.entity.AgentTaskDeadLetterEntity;

/**
 * AI 任务死信记录的数据访问接口。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@Mapper
public interface AgentTaskDeadLetterMapper extends BaseMapper<AgentTaskDeadLetterEntity> {
}