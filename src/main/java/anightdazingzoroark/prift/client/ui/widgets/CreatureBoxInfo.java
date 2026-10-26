package anightdazingzoroark.prift.client.ui.widgets;

import anightdazingzoroark.prift.server.entity.creature.IRiftCreature;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.util.function.Supplier;

/**
 * A passive hover card; creature actions remain in the slot's click menu.
 * */
public class CreatureBoxInfo extends Widget<CreatureBoxInfo> {
    private static final int CARD_WIDTH = 98;
    private static final int CARD_HEIGHT = 48;
    private static final int BAR_WIDTH = 86;
    @NotNull
    private final Supplier<IRiftCreature> creatureSupplier;

    public CreatureBoxInfo(
            @NotNull CreatureBoxSlot source, @NotNull Supplier<IRiftCreature> creatureSupplier,
            boolean openRight, boolean openUp
    ) {
        this.creatureSupplier = creatureSupplier;
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        int slotWidth = source.getArea().width;
        int slotHeight = source.getArea().height;
        boolean fitsRight = source.getArea().x() + slotWidth + CARD_WIDTH <= resolution.getScaledWidth();
        boolean fitsLeft = source.getArea().x() >= CARD_WIDTH;
        int x;
        int y;
        if (fitsRight && (openRight || !fitsLeft)) {
            x = source.getArea().x() + slotWidth;
            y = openUp ? source.getArea().y() + slotHeight - CARD_HEIGHT : source.getArea().y();
        }
        else if (fitsLeft) {
            x = source.getArea().x() - CARD_WIDTH;
            y = openUp ? source.getArea().y() + slotHeight - CARD_HEIGHT : source.getArea().y();
        }
        else {
            // On small GUI scales, move above or below the slot instead of covering it.
            x = source.getArea().x() + slotWidth / 2 - CARD_WIDTH / 2;
            if (source.getArea().y() >= CARD_HEIGHT) y = source.getArea().y() - CARD_HEIGHT;
            else y = source.getArea().y() + slotHeight;
        }
        this.size(CARD_WIDTH, CARD_HEIGHT).relativeToScreen().disableHoverBackground()
                .left(Math.clamp(x, 0, Math.max(0, resolution.getScaledWidth() - CARD_WIDTH)))
                .top(Math.clamp(y, 0, Math.max(0, resolution.getScaledHeight() - CARD_HEIGHT)));
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        IRiftCreature creature = this.creatureSupplier.get();
        if (creature == null) return;
        WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, false);
        new Rectangle().color(0xFF000000).cornerRadius(6).draw(context, 0, 0, CARD_WIDTH, CARD_HEIGHT, theme);
        new Rectangle().color(0xFFC6C6C6).cornerRadius(5).draw(context, 1, 1, CARD_WIDTH - 2, CARD_HEIGHT - 2, theme);
        IKey.str(creature.getName(false)).scale(0.5f).alignment(Alignment.CenterLeft)
                .draw(context, 6, 3, 86, 9, theme);
        IKey.lang("gui.prift.creature_summary.level", creature.getLevel()).scale(0.4f).alignment(Alignment.CenterLeft)
                .draw(context, 6, 11, 86, 9, theme);
        MoveIconTextures.drawCreatureElements(creature.getCreatureType().getElement(), 6, 21, 8);
        this.drawBar(context, theme, 32, creature.getHealth(), creature.getMaxHealth(), 0xFFFF0000);
        this.drawBar(context, theme, 37, creature.getStamina(), creature.getMaxStamina(), 0xFFFFFF00);
        this.drawBar(context, theme, 42, creature.getXP(), creature.getMaxXP(), 0xFF90EE90);
    }

    private void drawBar(
            @NotNull ModularGuiContext context, @NotNull WidgetTheme theme, int y,
            float value, float maximum, int color
    ) {
        int filledWidth = maximum <= 0f ? 0 : Math.clamp(Math.round(value / maximum * BAR_WIDTH), 0, BAR_WIDTH);
        new Rectangle().color(0xFF868686).draw(context, 6, y, BAR_WIDTH, 2, theme);
        if (filledWidth > 0) new Rectangle().color(color).draw(context, 6, y, filledWidth, 2, theme);
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFallback();
    }
}
