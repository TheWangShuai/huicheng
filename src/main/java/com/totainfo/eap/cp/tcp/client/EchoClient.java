package com.totainfo.eap.cp.tcp.client;


import com.totainfo.eap.cp.util.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Component
public class EchoClient{

    Map<String, ChannelFuture> futureMap = new ConcurrentHashMap<>();

    private static final long waitTime = 30;  //秒

    @Value("${tcp.client.separator}")
    private String separator;

    @Value("${tcp.client.retry.delay}")
    private long retryTime;   //重连间隔时间

    @Value("${tcp.timeout}")
    private long timeout;

    @Autowired
    private EchoClientHandler echoClientHandler;


    //启动方法
    public void start(String eqptId, String ip, int port) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
//                            为监听客户端read/write事件的Channel添加用户自定义的ChannelHandler
                    socketChannel.pipeline().addLast(new StringDecoder())
                            .addLast(new StringEncoder());
                    socketChannel.pipeline().addLast(echoClientHandler);
                }
            });
            //建立连接
            ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
            futureMap.put(eqptId, channelFuture);
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            LogUtils.info("设备:[" + eqptId + "]连接失败，原因", e);
        }finally {
            workerGroup.shutdownGracefully();
            try {
                TimeUnit.MILLISECONDS.sleep(retryTime);
                LogUtils.info("设备:[" + eqptId + "]开始重连。。。");
                start(eqptId, ip, port); // 断线重连
            } catch (InterruptedException e) {
                LogUtils.error("设备:[" + eqptId + "]重连异常", e);
            }
        }
    }

    public void send(String eqptId, String message){
        ChannelFuture future = futureMap.get(eqptId);
        if(future == null){
            LogUtils.error("设备:[" + eqptId + "]无法连接");
            return;
        }
        ByteBuf byteBuf = Unpooled.copiedBuffer((message+separator).getBytes());
        future.channel().writeAndFlush(byteBuf);
    }

    public String sendForReply(String eqptId, String message){
        ChannelFuture future = futureMap.get(eqptId);
        if(future == null){
            LogUtils.error("设备:[" + eqptId + "]无法连接");
            return null;
        }
        return null;
    }
}
