package com.taoqi.chat.task;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.taoqi.chat.api.enums.AgentTaskType;
import com.taoqi.chat.config.AgentTaskProperties;

/**
 * 模拟的 AI 文本生成器：用固定模板生成结果，并模拟一段耗时的模型调用。
 *
 * <p>接入真实模型时，只需把本类替换为真正的调用实现，任务状态流转与 MQ 链路无需改动。
 * 输入中包含「模拟失败」时主动抛异常，用于观察 MQ 重投与死信行为。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@Component
public class MockAiTextGenerator {

    private static final int EXCERPT_LENGTH = 40;

    @Resource
    private AgentTaskProperties agentTaskProperties;

    /**
     * 根据任务类型生成模拟结果。
     *
     * @param type  任务类型
     * @param input 原始输入
     * @return 模拟的生成结果
     */
    public String generate(AgentTaskType type, String input) {
        sleep(agentTaskProperties.getSimulateDelayMillis());
        if (input.contains("模拟失败")) {
            throw new IllegalStateException("模拟的模型调用失败");
        }

        String excerpt = excerpt(input);
        return switch (type) {
            case SUMMARY -> """
                    【模拟摘要】原文共 %d 字，主题围绕「%s」展开：
                    1. 先说明背景与目标，让读者快速建立上下文；
                    2. 再给出关键信息与结论，突出可执行的行动项；
                    3. 最后补充注意事项，降低理解与落地成本。

                    （模拟输出，接入真实模型后替换生成器即可）""".formatted(input.length(), excerpt);
            case COPYWRITING -> """
                    【模拟文案】主题：「%s」

                    让每一次表达，都更有力量。
                    · 简洁：一句话说清价值；
                    · 温度：让读者感到被理解；
                    · 记忆点：留下一个可复述的句子。

                    （模拟输出，接入真实模型后替换生成器即可）""".formatted(excerpt);
            case IDEA -> """
                    【模拟灵感】围绕「%s」的三个方向：
                    1. 场景化：把抽象概念放进具体使用场景；
                    2. 反差感：用出人意料的对比制造记忆点；
                    3. 参与感：留一个让读者动手或表达的空间。

                    （模拟输出，接入真实模型后替换生成器即可）""".formatted(excerpt);
        };
    }

    private String excerpt(String input) {
        String trimmed = input.trim();
        return trimmed.length() <= EXCERPT_LENGTH ? trimmed : trimmed.substring(0, EXCERPT_LENGTH) + "…";
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI 任务执行被中断", e);
        }
    }
}