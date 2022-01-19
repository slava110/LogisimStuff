package com.slava_110.logisimstuff.component.router;

import static com.slava_110.logisimstuff.component.Router.ATTRIBUTE_ADDRESS;
import static com.slava_110.logisimstuff.component.Router.ATTRIBUTE_PORT;

import java.net.InetSocketAddress;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceState;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class RouterControlClient extends RouterControl {
    @Nullable
    private EventLoopGroup workerGroup;
    @Nullable
    private ChannelFuture channelFuture;
    @Nullable
    private Value cachedVal;

    public RouterControlClient(){}

    private RouterControlClient(EventLoopGroup workerGroup, ChannelFuture channelFuture, Value cachedVal) {
        this.workerGroup = workerGroup;
        this.channelFuture = channelFuture;
        this.cachedVal = cachedVal;
    }

    @Override
    public void enable(InstanceState state) {
        updateCachedState(state);
        setRouterState(RouterState.ENABLING);

        workerGroup = new NioEventLoopGroup(1);

        try {
            Bootstrap bs = new Bootstrap();
            bs.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(
                            new InetSocketAddress(
                                    state.getAttributeValue(ATTRIBUTE_ADDRESS),
                                    state.getAttributeValue(ATTRIBUTE_PORT)
                            )
                    )
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(@NotNull NioSocketChannel ch) throws Exception {}
                    });
            channelFuture = bs.connect().addListener(fut -> {
                onInputUpdate(state.getPort(2));
                setRouterState(RouterState.IDLE);
            });
        } catch (Throwable e) {
            e.printStackTrace();
            setRouterState(RouterState.ERRORED);
        }
    }

    @Override
    public void onInputUpdate(Value value) {
        if(getRouterState() == RouterState.IDLE && value != cachedVal) {
            cachedVal = value;

            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer(encodeValue(value)));
        }
    }

    @Override
    public void disable(InstanceState state) {
        updateCachedState(state);
        if(getRouterState() != RouterState.DISABLING) {
            setRouterState(RouterState.DISABLING);

            sendDisableSignal();

            workerGroup.shutdownGracefully().addListener(fut -> {
                workerGroup = null;
                channelFuture = null;
                setRouterState(RouterState.DISABLED);
            });
        }
    }

    private void sendDisableSignal() {
        byte width = (byte) cachedVal.getWidth();
        byte[] data = new byte[width + 1];
        data[0] = width;
        for (int i = 0; i < width; i++) {
            data[i + 1] = -2;
        }

        channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer(data));
    }

    @Override
    public RouterControl clone() {
        return new RouterControlClient(workerGroup, channelFuture, cachedVal);
    }
}
