package com.taoqi.chat.api.enums;

/**
 * AI 任务类型，决定执行时使用哪种提示词模板。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
public enum AgentTaskType {

    /**
     * 文本总结。
     */
    SUMMARY("文本总结"),

    /**
     * 文案生成。
     */
    COPYWRITING("文案生成"),

    /**
     * 头脑风暴。
     */
    IDEA("头脑风暴");

    private final String description;

    AgentTaskType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}