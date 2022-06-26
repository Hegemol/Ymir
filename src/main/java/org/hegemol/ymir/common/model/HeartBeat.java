package org.hegemol.ymir.common.model;

import io.netty.channel.Channel;

/**
 * 心跳
 *
 * @author KevinClair
 **/
public class HeartBeat {

    /**
     * 通道信息
     */
    private Channel channel;

    /**
     * 重试次数
     */
    private int retryTimes;

    public HeartBeat() {
    }

    public HeartBeat(final Channel channel) {
        this.channel = channel;
        this.retryTimes = 0;
    }

    /**
     * Gets the value of channel.
     *
     * @return the value of channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Sets the channel.
     *
     * @param channel channel
     */
    public void setChannel(final Channel channel) {
        this.channel = channel;
    }

    /**
     * Gets the value of retryTimes.
     *
     * @return the value of retryTimes
     */
    public int getRetryTimes() {
        return retryTimes;
    }

    /**
     * Sets the retryTimes.
     *
     * @param retryTimes retryTimes
     */
    public void setRetryTimes(final int retryTimes) {
        this.retryTimes = retryTimes;
    }
}
