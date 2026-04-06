package anightdazingzoroark.prift.client.ui.widget;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.DrawableStack;
import com.cleanroommc.modularui.drawable.TabTexture;
import com.cleanroommc.modularui.theme.SelectableTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public class SideButton extends Widget<SideButton> implements Interactable {
    private IDrawable inactiveTexture = null;
    private boolean invert = false;
    private IGuiAction.MousePressed mousePressed;

    public SideButton() {
        this.disableHoverBackground();
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getToggleButtonTheme();
    }

    @Override
    protected WidgetTheme getActiveWidgetTheme(WidgetThemeEntry<?> widgetTheme, boolean hover) {
        return widgetTheme.expectType(SelectableTheme.class).getTheme(hover);
    }

    @Override
    public IDrawable getBackground() {
        return this.inactiveTexture;
    }

    @NotNull
    @Override
    public Result onMousePressed(int mouseButton) {
        if (this.mousePressed != null && this.mousePressed.press(mouseButton)) {
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    public SideButton background(boolean active, IDrawable... background) {
        if (active) return this.background(background);
        if (background.length == 0) this.inactiveTexture = null;
        else if (background.length == 1) this.inactiveTexture = background[0];
        else this.inactiveTexture = new DrawableStack(background);
        return this;
    }

    public SideButton tab(TabTexture texture, int location) {
        return this.background(this.invertSelected(), texture.get(location, invertSelected()))
                .background(!invertSelected(), texture.get(location, !invertSelected()))
                .disableHoverBackground()
                .size(texture.getWidth(), texture.getHeight());
    }

    public SideButton invertSelected(boolean invert) {
        this.invert = invert;
        return this.getThis();
    }

    public boolean invertSelected() {
        return this.invert;
    }

    public SideButton onMousePressed(IGuiAction.MousePressed mousePressed) {
        this.mousePressed = mousePressed;
        return getThis();
    }
}
