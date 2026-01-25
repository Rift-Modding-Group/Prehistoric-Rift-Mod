package anightdazingzoroark.prift.client.newui.custom;

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
    private IDrawable inactiveTexture = null;
    private boolean invert = false;

    public DynamicPageButton(int index, DynamicPagedWidget.Controller controller) {
        this.index = index;
        this.controller = controller;
        disableHoverBackground();
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getToggleButtonTheme();
    }

    @Override
    protected WidgetTheme getActiveWidgetTheme(WidgetThemeEntry<?> widgetTheme, boolean hover) {
        SelectableTheme selectableTheme = widgetTheme.expectType(SelectableTheme.class).getTheme(hover);
        return isActive() ^ invertSelected() ? selectableTheme.getSelected() : selectableTheme;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!isActive()) {
            this.controller.setPage(this.index);
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public IDrawable getBackground() {
        return isActive() || this.inactiveTexture == null ? super.getBackground() : this.inactiveTexture;
    }

    public boolean isActive() {
        return this.controller.getActivePageIndex() == this.index;
    }

    public DynamicPageButton background(boolean active, IDrawable... background) {
        if (active) {
            return background(background);
        }
        if (background.length == 0) {
            this.inactiveTexture = null;
        } else if (background.length == 1) {
            this.inactiveTexture = background[0];
        } else {
            this.inactiveTexture = new DrawableStack(background);
        }
        return this;
    }

    public DynamicPageButton tab(TabTexture texture, int location) {
        return background(invertSelected(), texture.get(location, invertSelected()))
                .background(!invertSelected(), texture.get(location, !invertSelected()))
                .disableHoverBackground()
                .size(texture.getWidth(), texture.getHeight());
    }

    public DynamicPageButton invertSelected(boolean invert) {
        this.invert = invert;
        return getThis();
    }

    public boolean invertSelected() {
        return this.invert;
    }
}
