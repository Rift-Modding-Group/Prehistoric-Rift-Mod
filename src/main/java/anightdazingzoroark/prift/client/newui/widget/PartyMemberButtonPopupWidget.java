package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PartyMemberButtonPopupWidget extends Widget<PartyMemberButtonPopupWidget> {
    private static final int[] buttonSize = new int[]{40, 8};
    private final List<IWidget> buttonList = new ArrayList<>();

    public PartyMemberButtonPopupWidget(CreatureNBT creatureNBT) {
        super();
        for (int index = 0; index < Option.values().length; index++) {
            Option option = Option.values()[index];
            Button buttonToAdd = new Button(creatureNBT, option);
            buttonToAdd.top(index * buttonSize[1]);
            this.buttonList.add(buttonToAdd);
        }
        this.size(buttonSize[0], buttonSize[1] * this.buttonList.size());
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());
        //bg
        new Rectangle().setColor(0xFF242424).draw(context, 0, 0, buttonSize[0], buttonSize[1] * this.buttonList.size(), theme);
    }


    @Override
    public @NotNull List<IWidget> getChildren() {
        return this.buttonList;
    }

    public enum Option {
        INVENTORY,
        OPTIONS,
        INFO,
        MOVES,
        SUMMON_OR_DISMISS,
        TELEPORT
    }

    public static class Button extends Widget<Button> implements Interactable {
        private final CreatureNBT buttonCreatureNBT;
        private final Option option;

        public Button(CreatureNBT buttonCreatureNBT, Option option) {
            super();
            this.size(buttonSize[0], buttonSize[1]);
            this.buttonCreatureNBT = buttonCreatureNBT;
            this.option = option;
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());
            float textScale = 0.5f;

            if (!this.buttonCreatureNBT.nbtIsEmpty()) {
                int hoveredColor = this.isHovering() ? 0xFF9F2666 : 0;

                //border
                new Rectangle().setColor(hoveredColor).draw(context, 0, 0, this.getArea().w(), this.getArea().h(), theme);

                //text
                IKey optionString = IKey.str(this.option.name()).alignment(Alignment.Center);
                optionString.scale(textScale)
                        .color(0xFFFFFFFF)
                        .draw(context, 0, 0,
                                this.getArea().w(),
                                this.getArea().h(),
                                theme
                        );
            }
        }
    }
}
