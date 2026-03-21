package anightdazingzoroark.prift.client.newui.panel;

import anightdazingzoroark.prift.client.newui.UIUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class RiftModularPanel extends ModularPanel {
    private Consumer<RiftModularPanel> openEffect;
    private Function<RiftModularPanel, Boolean> escEffect;

    public RiftModularPanel(@NotNull String name) {
        super(name);
    }

    public RiftModularPanel onOpen(@NotNull Consumer<RiftModularPanel> openEffect) {
        this.openEffect = openEffect;
        return this;
    }

    public RiftModularPanel onEscPressed(@NotNull Function<RiftModularPanel, Boolean> escEffect) {
        this.escEffect = Objects.requireNonNull(escEffect);
        return this;
    }

    @Override
    public void onInit() {
        super.onInit();

        if (this.openEffect != null) this.openEffect.accept(this);
    }

    @Override
    public boolean onKeyPressed(char typedChar, int keyCode) {
        if (this.escEffect != null) {
            if (UIUtils.canEscape(keyCode) && this.escEffect.apply(this)) return true;
        }
        return super.onKeyPressed(typedChar, keyCode);
    }
}
