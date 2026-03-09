package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.client.newui.RiftUIIcons;
import anightdazingzoroark.prift.client.newui.UIPanelNames;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.newui.value.HashMapValue;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.ObjectValue;
import com.cleanroommc.modularui.widgets.menu.ContextMenuButton;
import org.jetbrains.annotations.NotNull;

public class CreatureInBoxButtonWidget extends ContextMenuButton<CreatureInBoxButtonWidget> implements Interactable {
    @NotNull
    private final SelectedCreatureInfo.SelectedPosType section;
    private final int index;
    @NotNull
    private final BoolValue.Dynamic creatureSwitchingDynamic;
    @NotNull
    private final ObjectValue.Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic;
    @NotNull
    private final HashMapValue.Dynamic<Integer, RiftCreature> deployedPartyCreaturesDynamic;

    //party only stuff
    private PlayerPartyProperties playerParty;

    //creature box only stuff
    private PlayerCreatureBoxProperties playerBox;
    private IntValue.Dynamic currentBoxIndexDynamic;

    @NotNull
    private CreatureNBT creatureNBT = new CreatureNBT();
    private boolean isSelected;

    //the party only requires the index and the player
    public CreatureInBoxButtonWidget(
            PlayerPartyProperties playerParty,
            int index,
            BoolValue.@NotNull Dynamic creatureSwitchingDynamic,
            ObjectValue.@NotNull Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic,
            HashMapValue.@NotNull Dynamic<Integer, RiftCreature> deployedPartyCreaturesDynamic
    ) {
        super(UIPanelNames.BOX_DROPDOWN);
        this.section = SelectedCreatureInfo.SelectedPosType.PARTY;
        this.index = index;
        this.playerParty = playerParty;
        this.creatureSwitchingDynamic = creatureSwitchingDynamic;
        this.creatureSwapInfoDynamic = creatureSwapInfoDynamic;
        this.deployedPartyCreaturesDynamic = deployedPartyCreaturesDynamic;

        this.size(32);
    }

    //the box requires the box index, the index within the box, and the player
    public CreatureInBoxButtonWidget(
            PlayerCreatureBoxProperties playerBox,
            IntValue.Dynamic currentBoxIndexDynamic, int index,
            BoolValue.@NotNull Dynamic creatureSwitchingDynamic,
            ObjectValue.@NotNull Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic,
            HashMapValue.@NotNull Dynamic<Integer, RiftCreature> deployedPartyCreaturesDynamic
    ) {
        super(UIPanelNames.BOX_DROPDOWN);
        this.section = SelectedCreatureInfo.SelectedPosType.BOX;
        this.currentBoxIndexDynamic = currentBoxIndexDynamic;
        this.index = index;
        this.playerBox = playerBox;
        this.creatureSwitchingDynamic = creatureSwitchingDynamic;
        this.creatureSwapInfoDynamic = creatureSwapInfoDynamic;
        this.deployedPartyCreaturesDynamic = deployedPartyCreaturesDynamic;

        this.size(32);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.isValid()) return;

        this.creatureNBT = this.getCreatureNBT();
    }

    private CreatureNBT getCreatureNBT() {
        if (this.section == SelectedCreatureInfo.SelectedPosType.PARTY) {
            return this.playerParty.getPartyMember(this.index);
        }
        else if (this.section == SelectedCreatureInfo.SelectedPosType.BOX) {
            return this.playerBox.getCreatureBoxStorage().getBoxContents(this.currentBoxIndexDynamic.getIntValue()).get(this.index);
        }
        return new CreatureNBT();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());

        if (!this.creatureNBT.nbtIsEmpty()) {
            //outer outline, color changes depending on whether or not its hovered
            new Rectangle().color(this.getOutlineColor()).cornerRadius(5).drawAtZero(context, this.getArea(), theme);

            //inner outline
            new Rectangle().color(0xFF484848).cornerRadius(5).draw(context, 1, 1, this.getArea().w() - 2, this.getArea().h() - 2, theme);

            //set background
            new Rectangle().color(0xFF212121).cornerRadius(5).draw(context, 2, 2, this.getArea().w() - 4, this.getArea().h() - 4, theme);

            RiftUIIcons.creatureIcon(this.creatureNBT.getCreatureType()).draw(context, 4, 4, 24, 24, theme);
        }
        else new Rectangle().color(0xFF212121).cornerRadius(5).drawAtZero(context, this.getArea(), theme);
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFallback();
    }

    private int getOutlineColor() {
        if (this.isHovering()) return 0xFFFFFFFF;
        else if (this.isSelected) return 0xFFFFFF00;
        return 0xFF000000;
    }
}
