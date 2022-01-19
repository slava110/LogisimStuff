package com.slava_110.logisimstuff.component.router;

import static com.slava_110.logisimstuff.component.Router.ATTRIBUTE_ADDRESS;
import static com.slava_110.logisimstuff.component.Router.ATTRIBUTE_PORT;

import java.net.InetSocketAddress;

import org.jetbrains.annotations.NotNull;

import com.cburch.logisim.instance.InstanceState;
import com.slava_110.logisimstuff.component.router.handler.RouterChannelHandlerServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class RouterControlServer extends RouterControl {
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    public RouterControlServer(){}

    private RouterControlServer(EventLoopGroup workerGroup, ChannelFuture channelFuture) {
        this.workerGroup = workerGroup;
        this.channelFuture = channelFuture;
    }

    @Override
    public void enable(InstanceState state) {
        updateCachedState(state);
        setRouterState(RouterState.ENABLING);

        workerGroup = new NioEventLoopGroup(1);

        try {
            ServerBootstrap bs = new ServerBootstrap()
                    .channel(NioServerSocketChannel.class)
                    .group(workerGroup)
                    .localAddress(
                            new InetSocketAddress(
                                    state.getAttributeValue(ATTRIBUTE_ADDRESS),
                                    state.getAttributeValue(ATTRIBUTE_PORT)
                            )
                    )
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(@NotNull NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RouterChannelHandlerServer((val) -> {
                                state.setPort(2, val, 20);
                                state.fireInvalidated();
                            }));
                        }
                    });

            channelFuture = bs.bind().addListener(fut -> {
                setRouterState(RouterState.IDLE);
            });
        } catch (Throwable e) {
            e.printStackTrace();
            setRouterState(RouterState.ERRORED);
        }
    }

    @Override
    public void disable(InstanceState state) {
        updateCachedState(state);
        if(channelFuture != null && getRouterState() != RouterState.DISABLING) {
            setRouterState(RouterState.DISABLING);
            workerGroup.shutdownGracefully().addListener(fut -> {
                workerGroup = null;
                channelFuture = null;
                setRouterState(RouterState.DISABLED);
            });
        }
    }

    @Override
    public RouterControl clone() {
        return new RouterControlServer(workerGroup, channelFuture);
    }
}
