package com.slava_110.logisimstuff.data;

import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceDataSingleton;

public class ComponentDataSingleton<T> implements InstanceData, Cloneable {
    public T value;

    public ComponentDataSingleton(T value) {
        this.value = value;
    }

    @Override
    public InstanceDataSingleton clone() {
        try {
            return (InstanceDataSingleton) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
