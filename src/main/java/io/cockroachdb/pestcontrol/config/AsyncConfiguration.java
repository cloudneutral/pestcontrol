package io.cockroachdb.pestcontrol.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;

@Configuration
@EnableAspectJAutoProxy
@EnableAsync
@EnableScheduling
public class AsyncConfiguration implements AsyncConfigurer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int threadPoolSize = -1;

    @Override
    public AsyncTaskExecutor getAsyncExecutor() {
        return asyncTaskExecutor();
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Executor for @Async processing and app event multicasting.
     */
    @Bean(name = "asyncTaskExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        int poolSize = threadPoolSize > 0 ? threadPoolSize : Runtime.getRuntime().availableProcessors() * 4;
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize * 2);
        executor.setThreadNamePrefix("async-pool-");
        executor.setStrictEarlyShutdown(true);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setAcceptTasksAfterContextClose(false);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
        return executor;
    }

    @Bean("applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster(
            @Autowired @Qualifier("asyncTaskExecutor") Executor executor) {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(executor);
        eventMulticaster.setErrorHandler(t -> {
            logger.error("Unexpected error occurred in scheduled task", t);
        });
        return eventMulticaster;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            logger.error("Unexpected exception occurred invoking async method: " + method, ex);
        };
    }

    @Bean
    public CallableProcessingInterceptor callableProcessingInterceptor() {
        return new TimeoutCallableProcessingInterceptor();
    }
}

