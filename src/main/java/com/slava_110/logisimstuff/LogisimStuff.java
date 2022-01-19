package com.slava_110.logisimstuff;

import java.util.Arrays;
import java.util.List;

import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import com.slava_110.logisimstuff.component.fun.CatJAM;
import com.slava_110.logisimstuff.component.Router;
import com.slava_110.logisimstuff.component.informatics.CounterTable;
import com.slava_110.logisimstuff.component.informatics.FuncTable;
import com.slava_110.logisimstuff.component.informatics.KarnaughMap;

public class LogisimStuff extends Library {
    private static final List<Tool> tools = Arrays.asList(
            new AddTool(new CatJAM()),
            new AddTool(new Router()),
            new AddTool(new FuncTable()),
            new AddTool(new KarnaughMap()),
            new AddTool(new CounterTable())
    );

    @Override
    public String getName() {
        return "LogisimStuff";
    }

    @Override
    public String getDisplayName() {
        return "Logisim Stuff";
    }

    @Override
    public List<? extends Tool> getTools() {
        return tools;
    }
}
