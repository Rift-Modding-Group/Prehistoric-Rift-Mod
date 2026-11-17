package anightdazingzoroark.prift.client.ui.widgets;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.widgets.layout.Flow;

public class FlowUnhoverable extends Flow {
    public FlowUnhoverable(GuiAxis axis) {
        super(axis);
    }

    @Override
    public boolean canHover() {
        return false;
    }
}
