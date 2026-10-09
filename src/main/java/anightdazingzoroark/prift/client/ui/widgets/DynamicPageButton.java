package anightdazingzoroark.prift.client.ui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.DrawableStack;
import com.cleanroommc.modularui.drawable.TabTexture;
import com.cleanroommc.modularui.theme.SelectableTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public class DynamicPageButton extends Widget<DynamicPageButton> implements Interactable {
    private final int index;
    private final DynamicPagedWidget.Controller controller;
    private IDrawable inactiveTexture;

    public DynamicPageButton(int index, DynamicPagedWidget.Controller controller) {
        this.index = index;
        this.controller = controller;
        this.disableHoverBackground();
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getToggleButtonTheme();
    }

    @Override
    protected WidgetTheme getActiveWidgetTheme(WidgetThemeEntry<?> widgetTheme, boolean hover) {
        SelectableTheme selectableTheme = widgetTheme.expectType(SelectableTheme.class).getTheme(hover);
        return this.isActive() ? selectableTheme.getSelected() : selectableTheme;
    }

    @Override
    @NotNull
    public Result onMousePressed(int mouseButton) {
        if (!this.isActive()) {
            this.controller.setPage(this.index);
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public IDrawable getBackground() {
        return this.isActive() || this.inactiveTexture == null ? super.getBackground() : this.inactiveTexture;
    }

    private boolean isActive() {
        return this.controller.getActivePageIndex() == this.index;
    }

    private DynamicPageButton background(boolean active, IDrawable... background) {
        if (active) return this.background(background);

        if (background.length == 0) this.inactiveTexture = null;
        else if (background.length == 1) this.inactiveTexture = background[0];
        else this.inactiveTexture = new DrawableStack(background);
        return this;
    }

    public DynamicPageButton tab(TabTexture texture, int location) {
        return this.background(false, texture.get(location, false))
                .background(true, texture.get(location, true))
                .disableHoverBackground()
                .size(texture.getWidth(), texture.getHeight());
    }
}
