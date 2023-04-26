package com.example.interact.service.chatService.byNetty.config;


import com.example.interact.model.domain.ImUser;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储每个连接
 * @author W.G.
 */
@Resource
public class NettyConfig {
    /**
     * 储存每个客户端接入进来的channel对象,如果需要额外信息可以改为map
     */
    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static Map<ImUser, Channel> idChannelGroup = new ConcurrentHashMap<>() ;
    public static Map<Channel, ImUser>  channelIdGroup = new ConcurrentHashMap<>() ;
}