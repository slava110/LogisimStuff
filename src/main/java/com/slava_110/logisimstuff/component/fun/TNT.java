package com.slava_110.logisimstuff.component.fun;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Arrays;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.slava_110.logisimstuff.util.ComponentActions;
import com.slava_110.logisimstuff.util.ComponentUtils;

public class TNT extends InstanceFactory {

    public TNT() {
        super("tnt", () -> "TNT");

        setOffsetBounds(Bounds.create(-60, -60, 60, 60));

        Port[] ports = new Port[1];
        ports[0] = new Port(-60, -30, Port.INPUT, 1);
        ports[0].setToolTip(() -> "Explode");
        setPorts(ports);
    }

    private static final Image icon = ComponentUtils.loadImage("tnt.png");

    @Override
    public void paintInstance(InstancePainter painter) {
        // MC-like renderer
        TNTData data = ComponentUtils.getData(painter);

        Graphics g = painter.getGraphics();
        Bounds bounds = painter.getBounds();

        ComponentUtils.drawImage(painter, icon);

        if(data.primed) {
            data.timer--;
            float f2 = (1.0F - (data.timer + 1.0F) / 100.0F) * 0.8F;

            if (data.timer / 5 % 2 == 0) {
                g.setColor(new Color(1f, 1f, 1f, f2));
                g.fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
            }
            painter.fireInvalidated();

            if(data.timer <= 0)
                painter.getInstance().fireInvalidated();
        }

        painter.drawBounds();

        if(!data.primed)
            painter.drawPorts();
    }

    @Override
    public void propagate(InstanceState state) {
        TNTData data = ComponentUtils.getOrCreateData(state, TNTData::new);
        if(!data.primed && state.getPort(0) == Value.TRUE) {
            data.primed = true;
        }

        if(data.primed && data.timer <= 0)
            ComponentActions.removeComponents(state.getProject(), Arrays.asList(Instance.getComponentFor(state.getInstance())));
    }

    private static class TNTData implements InstanceData {
        public boolean primed = false;
        public int timer = 50;

        public TNTData() {}

        private TNTData(boolean enabled, int timer) {
            this.primed = enabled;
            this.timer = timer;
        }

        @Override
        public Object clone() {
            return new TNTData(primed, timer);
        }
    }
}
