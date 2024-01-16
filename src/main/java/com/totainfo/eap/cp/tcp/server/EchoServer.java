package com.totainfo.eap.cp.tcp.server;

import com.totainfo.eap.cp.util.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/*
 * 服务端
 * 1.创建一个ServerBootstrap的实例引导和绑定服务器。
 * 2.创建并分配一个NioEventLoopGroup实例以进行事件的处理，比如接受连接以及读写数据。
 * 3.指定服务器绑定的本地的InetSocketAddress。
 * 4.使用一个EchoServerHandler的实例初始化每一个新的Channel。
 * 5.调用ServerBootstrap.bind()方法以绑定服务器。
 */

@Component
public class EchoServer {

    /*
     * NioEventLoop并不是一个纯粹的I/O线程，它除了负责I/O的读写之外
     * 创建了两个NioEventLoopGroup，
     * 它们实际是两个独立的Reactor线程池。
     * 一个用于接收客户端的TCP连接，
     * 另一个用于处理I/O相关的读写操作，或者执行系统Task、定时任务Task等。
     */

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;
    /*
     * 启动服务
     * @param port
     * @return
     * @throws Exception
     */

    @Autowired
    private EchoServerHandler echoServerHandler;

    public ChannelFuture start(int port){

        ChannelFuture f = null;
        try {
            //ServerBootstrap负责初始化netty服务器，并且开始监听端口的socket请求
            //辅助启动nio服务的类
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    //设置服务端的channel类型,我们使用的是nio,所以是NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    //设置childHandler具体需要执行的处理器,可以帮助使用一些自己的handler处理自己的网络程序
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
//                            为监听客户端read/write事件的Channel添加用户自定义的ChannelHandler
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new StringEncoder());
                            socketChannel.pipeline().addLast(echoServerHandler);
                        }
                    });

            f = b.bind().sync();
            channel = f.channel();
        } catch (Exception e) {
            LogUtils.error("穿件Socket Server 异常", e);
        } finally {
        }
        return f;
    }

    /*
     * 停止服务
     */

    public void destroy() {
        if(channel != null) {
            channel.close();
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
