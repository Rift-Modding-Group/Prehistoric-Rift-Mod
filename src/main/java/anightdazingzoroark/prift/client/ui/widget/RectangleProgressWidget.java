package anightdazingzoroark.prift.client.ui.widget;

import com.cleanroommc.modularui.api.value.IDoubleValue;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;

public class RectangleProgressWidget extends Widget<RectangleProgressWidget> {
    private IDoubleValue<?> doubleValue;
    private int borderColor = 0xFF000000;
    private int innerColor = 0xFF808080;
    private int valueColor = 0xFFFF0000;

    @Override
    public void onInit() {
        super.onInit();
        if (this.doubleValue == null) {
            this.doubleValue = new DoubleValue(0.5);
        }
    }

    public RectangleProgressWidget borderColor(int value) {
        this.borderColor = value;
        return this;
    }

    public RectangleProgressWidget innerColor(int value) {
        this.innerColor = value;
        return this;
    }

    public RectangleProgressWidget valueColor(int value) {
        this.valueColor = value;
        return this;
    }

    public RectangleProgressWidget setValue(IDoubleValue<?> value) {
        this.doubleValue = value;
        return this;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());

        //create border
        new Rectangle().color(this.borderColor).drawAtZero(context, this.getArea(), theme);

        //create inner color
        Area innerColor = new Area(this.getArea());
        innerColor.w(this.getArea().w() - 2);
        innerColor.h(this.getArea().h() - 2);
        new Rectangle().color(this.innerColor).draw(context, 1, 1, innerColor.w(), innerColor.h(), theme);

        //create contents
        Area contentsColor = new Area(innerColor);
        contentsColor.w((int) (innerColor.w() * this.doubleValue.getDoubleValue()));
        new Rectangle().color(this.valueColor).draw(context, 1, 1, contentsColor.w(), contentsColor.h(), theme);
    }
}
