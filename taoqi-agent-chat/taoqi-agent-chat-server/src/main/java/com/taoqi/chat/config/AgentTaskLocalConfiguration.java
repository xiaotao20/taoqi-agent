package com.taoqi.chat.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 本地任务执行线程池：只在 {@code dispatch-mode=local} 时注册。
 *
 * <p>有界队列加 {@code CallerRunsPolicy}，队列满时由提交线程执行，避免任务被静默丢弃。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "taoqi.chat.agent-task", name = "dispatch-mode", havingValue = "local")
public class AgentTaskLocalConfiguration {

    @Bean(name = "agentTaskLocalExecutor")
    public ThreadPoolTaskExecutor agentTaskLocalExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("agent-task-local-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }
}