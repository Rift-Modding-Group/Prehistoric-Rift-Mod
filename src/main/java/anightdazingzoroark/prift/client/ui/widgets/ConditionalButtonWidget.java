package anightdazingzoroark.prift.client.ui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class ConditionalButtonWidget extends ButtonWidget<ConditionalButtonWidget> {
    @NotNull
    private final BooleanSupplier enabledCondition;

    public ConditionalButtonWidget(@NotNull BooleanSupplier enabledCondition) {
        this.enabledCondition = enabledCondition;
    }

    @Override
    @NotNull
    public Result onMousePressed(int mouseButton) {
        if (!this.enabledCondition.getAsBoolean()) return Result.STOP;
        return super.onMousePressed(mouseButton);
    }

    @Override
    @Nullable
    public IDrawable getThemeBackground(WidgetThemeEntry<?> widgetTheme) {
        if (!this.enabledCondition.getAsBoolean()) return GuiTextures.MC_BUTTON_DISABLED;
        return super.getThemeBackground(widgetTheme);
    }
}
