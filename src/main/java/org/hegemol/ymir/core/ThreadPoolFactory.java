package org.hegemol.ymir.core;

import org.hegemol.ymir.common.utils.YmirThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工厂.
 */
public class ThreadPoolFactory {

    /**
     * 公用线程池
     */
    private static final Executor EXECUTOR = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors() * 2, 3000, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(), new YmirThreadFactory("works"), new ThreadPoolExecutor.AbortPolicy());

     /**
     * 提交任务执行
     *
     * @param runnable 线程任务.
     */
    public static void execute(Runnable runnable){
        EXECUTOR.execute(runnable);
    }

    /**
     * 获得线程池
     *
     * @return executor
     */
    public static Executor getExecutor(){
        return EXECUTOR;
    }
}
