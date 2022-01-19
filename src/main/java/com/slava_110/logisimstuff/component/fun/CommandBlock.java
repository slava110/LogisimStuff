package com.slava_110.logisimstuff.component.fun;

import java.awt.Image;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.slava_110.logisimstuff.data.ComponentDataSingleton;
import com.slava_110.logisimstuff.util.ComponentUtils;

public class CommandBlock extends InstanceFactory {
    private static final Attribute<String> ATTRIBUTE_COMMAND = Attributes.forString("command", () -> "Command");

    public CommandBlock() {
        super("command_block", () -> "Command Block");
        setOffsetBounds(Bounds.create(-60, -60, 60, 60));

        Port[] ports = new Port[2];
        ports[0] = new Port(-60, -30, Port.INPUT, 1);
        ports[0].setToolTip(() -> "Execute command");
        ports[1] = new Port(0, -30, Port.OUTPUT, 1);
        ports[1].setToolTip(() -> "Execution result");
        setPorts(ports);

        setAttributes(
                new Attribute[]{
                        ATTRIBUTE_COMMAND
                },
                new Object[]{
                        ""
                }
        );
    }

    private static final Image gif = ComponentUtils.loadImage("command_block.gif");

    @Override
    public void paintInstance(InstancePainter painter) {
        ComponentUtils.drawImage(painter, gif);

        painter.drawBounds();
        painter.drawPorts();
    }

    @Override
    public void propagate(InstanceState state) {
        ComponentDataSingleton<Boolean> data = ComponentUtils.getOrCreateData(state, () -> new ComponentDataSingleton<Boolean>(false));
        if(state.getPort(0) == Value.TRUE) {
            if(!data.value) {
                state.setPort(1, executeCommand(state.getAttributeValue(ATTRIBUTE_COMMAND), state), 20);
                data.value = true;
            }
        } else if(data.value) {
            data.value = false;
        }
    }

    private static Value executeCommand(String commandRaw, InstanceState state) {
        String[] command = commandRaw.split(" ");
        switch (command[0]) {
            case "tick":
                state.getProject().getSimulator().tick();
                break;
            case "ticking":
                state.getProject().getSimulator().setIsTicking(!state.getProject().getSimulator().isTicking());
                break;
            case "tickrate":
                try {
                    double tickrate = Double.parseDouble(command[1]);
                    state.getProject().getSimulator().setTickFrequency(Math.min(Math.max(0.25D, tickrate), 4100D));
                } catch (NumberFormatException e) {
                    return Value.ERROR;
                }
                break;
            case "reset":
                state.getProject().getSimulator().requestReset();
                break;
            default:
                return Value.FALSE;
        }

        return Value.TRUE;
    }
}
