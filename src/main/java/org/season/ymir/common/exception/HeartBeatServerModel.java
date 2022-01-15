package org.season.ymir.common.exception;

import io.netty.channel.Channel;

import java.util.Date;

/**
 * 服务端心跳保持
 *
 * @author KevinClair
 **/
public class HeartBeatServerModel {

    // Channel管理器
    private Channel channel;

    // 最后一次心跳事件
    private Date lastHeartBeatTime;

    public HeartBeatServerModel() {
    }

    public HeartBeatServerModel(final Channel channel, final Date lastHeartBeatTime) {
        this.channel = channel;
        this.lastHeartBeatTime = lastHeartBeatTime;
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
     * Gets the value of lastHeartBeatTime.
     *
     * @return the value of lastHeartBeatTime
     */
    public Date getLastHeartBeatTime() {
        return lastHeartBeatTime;
    }

    /**
     * Sets the lastHeartBeatTime.
     *
     * @param lastHeartBeatTime lastHeartBeatTime
     */
    public void setLastHeartBeatTime(final Date lastHeartBeatTime) {
        this.lastHeartBeatTime = lastHeartBeatTime;
    }
}
