package com.taoqi.chat.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * AI 任务提交请求。
 *
 * @param taskType 任务类型，为空时默认 {@code COPYWRITING}
 * @param input    任务输入，例如一段需要总结或改写的文本
 * @author xiaotao
 * @date 2026-11-04
 */
public record AgentTaskSubmitRequest(

        @Pattern(regexp = "SUMMARY|COPYWRITING|IDEA", message = "任务类型仅支持 SUMMARY、COPYWRITING、IDEA")
        String taskType,

        @NotBlank(message = "输入内容不能为空")
        @Size(max = 2000, message = "输入内容不能超过 2000 字")
        String input) {
}