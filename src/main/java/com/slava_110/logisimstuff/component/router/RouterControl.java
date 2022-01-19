package com.slava_110.logisimstuff.component.router;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceState;
import com.slava_110.logisimstuff.LogisimUtils;

public abstract class RouterControl implements InstanceData {
    private InstanceState cachedInstanceState;
    private RouterState routerState = RouterState.DISABLED;

    public abstract void enable(InstanceState state);

    public RouterState getRouterState() {
        return routerState;
    }

    protected void setRouterState(RouterState routerState) {
        if(this.routerState != routerState) {
            this.routerState = routerState;

            if(cachedInstanceState != null) {
                Value val = cachedInstanceState.getPort(3);

                if(val != routerState.value) {
                    cachedInstanceState.setPort(3, routerState.value, 20);
                    cachedInstanceState.fireInvalidated();
                }
            }
        }
    }

    public void onInputUpdate(Value value) {}

    public abstract void disable(InstanceState state);

    protected void updateCachedState(InstanceState state) {
        if(cachedInstanceState != state) {
            cachedInstanceState = state;
        }
    }

    @Override
    public abstract RouterControl clone();

    public static byte[] encodeValue(Value val) {
        byte width = (byte) val.getWidth();

        byte[] encoded = new byte[width + 1];
        encoded[0] = width;

        for (byte i = 0; i < width; i++) {
            encoded[i + 1] = (byte) LogisimUtils.getIntFromValue(val.get(i));
        }
        return encoded;
    }

    public static Value decodeValue(byte[] encoded) {
        byte width = encoded[0];

        Value[] decoded = new Value[width];

        for (byte i = 0; i < width; i++) {
            decoded[i] = LogisimUtils.createValueFromInt(encoded[i + 1]);
        }
        return Value.create(decoded);
    }

    public static enum RouterState {
        DISABLED(0, 0),
        ENABLING(1, 0),
        IDLE(1, 1),
        DISABLING(0, 1),
        ERRORED(-1, -1);
        public final Value value;

        RouterState(int a, int b) {
            this.value = LogisimUtils.createValueFromInts(a, b);
        }

        public boolean isEnabled() {
            return this == RouterState.IDLE;
        }
    }
}
