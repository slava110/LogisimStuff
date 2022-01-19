package com.slava_110.logisimstuff.component;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.slava_110.logisimstuff.component.router.RouterControl;
import com.slava_110.logisimstuff.component.router.RouterControl.RouterState;
import com.slava_110.logisimstuff.component.router.RouterControlClient;
import com.slava_110.logisimstuff.component.router.RouterControlServer;

public class Router extends InstanceFactory {
    public static final AttributeOption ATTRIBUTE_TYPE_CLIENT = new AttributeOption("client", () -> "Client");
    public static final AttributeOption ATTRIBUTE_TYPE_SERVER = new AttributeOption("server", () -> "Server");

    public static final Attribute<AttributeOption> ATTRIBUTE_TYPE = Attributes.forOption("type", () -> "Type", new AttributeOption[]{
            ATTRIBUTE_TYPE_CLIENT,
            ATTRIBUTE_TYPE_SERVER
    });
    public static final Attribute<String> ATTRIBUTE_ADDRESS = Attributes.forString("address", () -> "Address");
    public static final Attribute<Integer> ATTRIBUTE_PORT = Attributes.forInteger("port", () -> "Port");

    public Router() {
        super("router", () -> "Router");
        setOffsetBounds(Bounds.create(-120, -120, 120, 120));

        Port[] ports = new Port[4];
        ports[0] = new Port(-120, -30, Port.INPUT, 1);
        ports[0].setToolTip(() -> "Enable router");
        ports[1] = new Port(-120, -60, Port.INPUT, 8);
        ports[1].setToolTip(() -> "Data input");
        ports[2] = new Port(0, -60, Port.OUTPUT, 8);
        ports[2].setToolTip(() -> "Data output");
        ports[3] = new Port(-30, -120, Port.OUTPUT, 2);
        ports[3].setToolTip(() -> "Connection status");

        setPorts(ports);

        setAttributes(
                new Attribute[]{
                        ATTRIBUTE_TYPE,
                        ATTRIBUTE_ADDRESS,
                        ATTRIBUTE_PORT
                },
                new Object[]{
                        ATTRIBUTE_TYPE_CLIENT,
                        "",
                        0
                }
        );
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawBounds();
        painter.drawPorts();
    }

    @Override
    public void propagate(InstanceState state) {
        Value portValue = state.getPort(0);
        RouterControl control = (RouterControl) state.getData();
        if(portValue == Value.TRUE) {
            if(control == null) {
                control = state.getAttributeValue(ATTRIBUTE_TYPE) == ATTRIBUTE_TYPE_CLIENT ? new RouterControlClient() : new RouterControlServer();
                state.setData(control);
            }

            if(control.getRouterState() == RouterState.DISABLED) {
                control.enable(state);
            } else if(control.getRouterState() == RouterState.IDLE) {
                control.onInputUpdate(state.getPort(1));
            }
        } else if(portValue == Value.FALSE) {
            if(control != null && control.getRouterState() != RouterState.DISABLED) {
                control.disable(state);
            }
        }
    }
}
