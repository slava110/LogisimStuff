package com.slava_110.logisimstuff.component.router.handler;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.cburch.logisim.data.Value;
import com.slava_110.logisimstuff.component.router.RouterControl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RouterChannelHandlerServer extends SimpleChannelInboundHandler<ByteBuf> {
    private final Consumer<Value> handler;

    public RouterChannelHandlerServer(Consumer<Value> handler) {
        this.handler = handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        handler.accept(RouterControl.decodeValue(ByteBufUtil.getBytes(msg)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
