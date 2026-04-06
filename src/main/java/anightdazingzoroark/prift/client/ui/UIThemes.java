package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.ThemeBuilder;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeKey;

public class UIThemes {
    public static final WidgetThemeKey<WidgetTheme> JOURNAL_PANEL = IThemeApi.get().widgetThemeKeyBuilder("journal_panel", WidgetTheme.class)
            .defaultTheme(WidgetTheme.darkTextNoShadow(400, 240, new JournalPanel()))
            .register();

    public static final ThemeBuilder<?> JOURNAL_THEME = new ThemeBuilder<>(RiftInitialize.MODID+":journal_theme")
            .defaultTextColor(0xFFFF0000);

    private static class JournalPanel implements IDrawable {
        @Override
        public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
            new Rectangle().color(0xFF000000).cornerRadius(5).draw(context, x, y, width, height, widgetTheme);
            new Rectangle().color(0xFF492b16).cornerRadius(5).draw(context, x + 1, y + 1, width - 2, height - 2, widgetTheme);
            new Rectangle().color(0xFF603A22).cornerRadius(5).draw(context, x + 4, y + 4, width - 8, height - 8, widgetTheme);
            new Rectangle().color(0xFFCCB998).cornerRadius(5).draw(context, x + 5, y + 5, width - 10, height - 10, widgetTheme);

            //separator
            new Rectangle().color(0xFF6F462A).draw(context, width / 2 - 1, y + 5, 2, height - 10, widgetTheme);
        }
    }
}
