package com.taoqi.integration.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步执行配置：为邮件发送提供独立线程池。
 *
 * <p>SMTP 发送是阻塞 IO，用有界线程池控制并发；队列满时由调用线程执行（{@code CallerRunsPolicy}），
 * 避免任务丢失，同时形成天然背压。关闭时等待在途任务完成，避免记录停留在“发送中”。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Configuration(proxyBeanMethods = false)
@EnableAsync
public class AsyncConfig {

    @Bean(name = "emailSendExecutor")
    public ThreadPoolTaskExecutor emailSendExecutor(EmailAsyncProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.corePoolSize());
        executor.setMaxPoolSize(properties.maxPoolSize());
        executor.setQueueCapacity(properties.queueCapacity());
        executor.setThreadNamePrefix("email-send-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }
}
