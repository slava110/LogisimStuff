package com.slava_110.logisimstuff.component.fun;

import static com.slava_110.logisimstuff.util.ComponentUtils.getData;
import static com.slava_110.logisimstuff.util.ComponentUtils.getOrCreateData;

import java.awt.Image;
import java.awt.event.MouseEvent;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.slava_110.logisimstuff.data.ComponentDataSingleton;
import com.slava_110.logisimstuff.util.ComponentUtils;

public class CatJAM extends InstanceFactory {

    public CatJAM() {
        super("catjam", () -> "Cat JAM");
        setOffsetBounds(Bounds.create(-120, -120, 120, 120));
        setPorts(new Port[]{
                new Port(-120, -60, Port.INPUT, 1)
        });

        setInstancePoker(CatJAMPoker.class);
    }

    private static final Image gif = ComponentUtils.loadImage("cat-jam.gif");

    @Override
    public void paintInstance(InstancePainter painter) {
        ComponentDataSingleton<Boolean> data = getData(painter);
        if(data != null && data.value) {
            ComponentUtils.drawImage(painter, gif);
        }
        painter.fireInvalidated();

        painter.drawBounds();
        painter.drawPorts();
    }

    @Override
    public void propagate(InstanceState state) {
        Value portValue = state.getPort(0);
        if(portValue == Value.TRUE) {
            getOrCreateData(state, () -> new ComponentDataSingleton<>(false)).value = true;
        } else if(portValue == Value.FALSE) {
            getOrCreateData(state, () -> new ComponentDataSingleton<>(false)).value = false;
        }
    }

    public static class CatJAMPoker extends InstancePoker {

        @Override
        public void mouseReleased(InstanceState state, MouseEvent e) {
            if(!state.getPort(0).isFullyDefined()) {
                ComponentDataSingleton<Boolean> data = getOrCreateData(state, () -> new ComponentDataSingleton<>(false));
                data.value = !data.value;
                state.getInstance().fireInvalidated();
            }
        }
    }
}
