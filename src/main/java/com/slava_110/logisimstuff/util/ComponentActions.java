package com.slava_110.logisimstuff.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.circuit.CircuitTransactionResult;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.proj.Project;

public class ComponentActions {

    public static CircuitTransactionResult addComponents(Project proj, List<Component> components) {
        return executeMutation(proj, (circuit, xn) -> {
            xn.addAll(components);
        });
    }

    public static CircuitTransactionResult removeComponents(Project proj, List<Component> components) {
        return executeMutation(proj, (circuit, xn) -> {
            for (Component comp : components) {
                xn.remove(comp);
            }
        });
    }

    public static CircuitTransactionResult moveComponents(Project proj, List<Component> components, int dx, int dy) {
        return executeMutation(proj, (circuit, xn) -> {
            Map<Component, Component> moved = copyComponents(components, dx, dy);

            for (Map.Entry<Component, Component> en : moved.entrySet()) {
                xn.replace(en.getKey(), en.getValue());
            }
        });
    }

    private static Map<Component, Component> copyComponents(List<Component> components, int dx, int dy) {
        Map<Component,Component> ret = new HashMap<>();
        for (Component comp : components) {
            Location oldLoc = comp.getLocation();
            AttributeSet attrs = (AttributeSet) comp.getAttributeSet().clone();
            int newX = oldLoc.getX() + dx;
            int newY = oldLoc.getY() + dy;
            Object snap = comp.getFactory().getFeature(ComponentFactory.SHOULD_SNAP, attrs);
            if (snap == null || ((Boolean) snap).booleanValue()) {
                newX = Canvas.snapXToGrid(newX);
                newY = Canvas.snapYToGrid(newY);
            }
            Location newLoc = Location.create(newX, newY);

            Component copy = comp.getFactory().createComponent(newLoc, attrs);
            ret.put(comp, copy);
        }
        return ret;
    }

    private static CircuitTransactionResult executeMutation(Project proj, BiConsumer<Circuit, CircuitMutation> mutation) {
        Circuit circuit = proj.getCurrentCircuit();
        CircuitMutation xn = new CircuitMutation(circuit);

        mutation.accept(circuit, xn);

        return xn.execute();
    }
}
