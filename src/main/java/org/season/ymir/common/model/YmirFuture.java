package org.season.ymir.common.model;

import java.util.concurrent.*;

/**
 * Future对象
 *
 * @author KevinClair
 */
public class YmirFuture<T> implements Future<T> {

    private T response;
    /**
     * 因为请求和响应是一一对应的，所以这里是1
     */
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        if (response != null) {
            return true;
        }
        return false;
    }

    /**
     * 获取响应，直到有结果才返回
     *
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */

    @Override
    public T get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return response;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!countDownLatch.await(timeout,unit)){
            throw new TimeoutException();
        }
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
        countDownLatch.countDown();
    }
}
