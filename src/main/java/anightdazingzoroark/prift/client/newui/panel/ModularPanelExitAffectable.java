package anightdazingzoroark.prift.client.newui.panel;

import anightdazingzoroark.prift.client.newui.UIUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.util.Objects;
import java.util.function.Function;

public class ModularPanelExitAffectable extends ModularPanel {
    private Function<ModularPanelExitAffectable, Boolean> escEffect = panel -> false;

    public ModularPanelExitAffectable(@NotNull String name) {
        super(name);
    }

    public ModularPanelExitAffectable onEscPressed(@NotNull Function<ModularPanelExitAffectable, Boolean> escEffect) {
        this.escEffect = Objects.requireNonNull(escEffect);
        return this;
    }

    @Override
    public boolean onKeyPressed(char typedChar, int keyCode) {
        if (UIUtils.canEscape(keyCode) && this.escEffect.apply(this)) return true;
        return super.onKeyPressed(typedChar, keyCode);
    }
}
