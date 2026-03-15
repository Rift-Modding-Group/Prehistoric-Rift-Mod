package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.client.newui.RiftUIIcons;
import anightdazingzoroark.prift.client.newui.UIColors;
import anightdazingzoroark.prift.client.newui.UIPanelNames;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.newui.value.FixedSizeCreatureListSyncValue;
import anightdazingzoroark.prift.client.newui.value.HashMapValue;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.DrawableStack;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.ObjectValue;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.menu.AbstractMenuButton;
import com.cleanroommc.modularui.widgets.menu.ContextMenuButton;
import com.cleanroommc.modularui.widgets.menu.Menu;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Consumer;

public class CreatureInBoxButtonWidget extends ContextMenuButton<CreatureInBoxButtonWidget> implements Interactable {
    @NotNull
    private final SelectedCreatureInfo.SelectedPosType section;
    private final int index;
    @NotNull
    private final ObjectValue.Dynamic<SelectedCreatureInfo> selectedCreatureInfoDynamic;
    @NotNull
    private final BoolValue.Dynamic creatureSwitchingDynamic;
    @NotNull
    private final ObjectValue.Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic;
    @NotNull
    private final SelectedCreatureInfo selectedCreatureInfo;

    //party only stuff
    private PlayerPartyProperties playerParty;

    //creature box only stuff
    private PlayerCreatureBoxProperties playerBox;
    private IntValue.Dynamic currentBoxIndexDynamic;

    //deployed from box only stuff
    private FixedSizeCreatureListSyncValue boxDeployedCreatures;

    //other stuff that changes
    @NotNull
    private CreatureNBT creatureNBT = new CreatureNBT();
    private boolean isSelected;

    //the party only requires the index and the player
    public CreatureInBoxButtonWidget(
            PlayerPartyProperties playerParty,
            int index,
            ObjectValue.@NotNull Dynamic<SelectedCreatureInfo> selectedCreatureInfoDynamic,
            BoolValue.@NotNull Dynamic creatureSwitchingDynamic,
            ObjectValue.@NotNull Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic
    ) {
        super(UIPanelNames.BOX_DROPDOWN+":party:"+index);
        this.section = SelectedCreatureInfo.SelectedPosType.PARTY;
        this.index = index;
        this.playerParty = playerParty;
        this.selectedCreatureInfoDynamic = selectedCreatureInfoDynamic;
        this.creatureSwitchingDynamic = creatureSwitchingDynamic;
        this.creatureSwapInfoDynamic = creatureSwapInfoDynamic;

        this.selectedCreatureInfo = SelectedCreatureInfo.partySelectedInfo(index);

        this.commonSetup();
    }

    //the box requires the box index, the index within the box, and the player
    public CreatureInBoxButtonWidget(
            PlayerCreatureBoxProperties playerBox,
            IntValue.Dynamic currentBoxIndexDynamic, int index,
            ObjectValue.@NotNull Dynamic<SelectedCreatureInfo> selectedCreatureInfoDynamic,
            BoolValue.@NotNull Dynamic creatureSwitchingDynamic,
            ObjectValue.@NotNull Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic
    ) {
        super(UIPanelNames.BOX_DROPDOWN+":box:"+index);
        this.section = SelectedCreatureInfo.SelectedPosType.BOX;
        this.currentBoxIndexDynamic = currentBoxIndexDynamic;
        this.index = index;
        this.playerBox = playerBox;
        this.selectedCreatureInfoDynamic = selectedCreatureInfoDynamic;
        this.creatureSwitchingDynamic = creatureSwitchingDynamic;
        this.creatureSwapInfoDynamic = creatureSwapInfoDynamic;

        this.selectedCreatureInfo = SelectedCreatureInfo.boxSelectedInfoDynamic(currentBoxIndexDynamic, index);

        this.commonSetup();
    }

    //deployed from box requires a FixedSizeCreatureListSyncValue which has the box's
    //deployed creatures
    public CreatureInBoxButtonWidget(
            FixedSizeCreatureListSyncValue boxDeployedCreatures,
            int index,
            ObjectValue.@NotNull Dynamic<SelectedCreatureInfo> selectedCreatureInfoDynamic,
            BoolValue.@NotNull Dynamic creatureSwitchingDynamic,
            ObjectValue.@NotNull Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic
    ) {
        super(UIPanelNames.BOX_DROPDOWN+":boxdeployed:"+index);
        this.section = SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED;
        this.boxDeployedCreatures = boxDeployedCreatures;
        this.index = index;
        this.selectedCreatureInfoDynamic = selectedCreatureInfoDynamic;
        this.creatureSwitchingDynamic = creatureSwitchingDynamic;
        this.creatureSwapInfoDynamic = creatureSwapInfoDynamic;

        this.selectedCreatureInfo = SelectedCreatureInfo.boxDeployedInfo(index);

        this.commonSetup();
    }

    private void commonSetup() {
        this.size(32);
        this.menu(new MenuForCreature(this));
        this.openCustom();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.isValid()) return;

        this.isSelected = this.selectedCreatureInfo.equals(this.selectedCreatureInfoDynamic.getValue());
        this.creatureNBT = this.getCreatureNBT();
    }

    @Override
    @NotNull
    public Result onMousePressed(int mouseButton) {
        //only clickable really (but in stricter conditions) when swapping
        if (this.creatureNBT.nbtIsEmpty()) {
            if (this.creatureSwitchingDynamic.getBoolValue()
                    && this.creatureSwapInfoDynamic.getValue().canSwapHalfway()
            ) {
                this.creatureSwapInfoDynamic.getValue().setCreature(this.selectedCreatureInfo);
                return Result.SUCCESS;
            }
            else return Result.ACCEPT;
        }
        else {
            //this is to make sure that the relationship between selectedCreatureInfoDynamic
            //and selectedCreatureInfo results in isSelected being true or false
            if (this.isSelected) this.selectedCreatureInfoDynamic.setValue(null);
            else this.selectedCreatureInfoDynamic.setValue(this.selectedCreatureInfo);

            //swapping related operations
            if (!this.creatureSwitchingDynamic.getBoolValue()) return super.onMousePressed(mouseButton);

            if (!this.creatureSwapInfoDynamic.getValue().canSwap()) {
                this.creatureSwapInfoDynamic.getValue().setCreature(this.selectedCreatureInfo);
            }
            return Result.SUCCESS;
        }
    }

    //this is to ensure that, only when there is creatureNBT and nothing else
    //is selected and when not swapping creatures can the hover menu be opened
    @Override
    public void onMouseEnterArea() {
        if (!this.creatureNBT.nbtIsEmpty()
                && this.selectedCreatureInfoDynamic.getValue() == null
                && !this.creatureSwitchingDynamic.getBoolValue()
        ) {
            super.onMouseEnterArea();
        }
    }

    //this is to ensure that, only when there is creatureNBT and is not selected, can
    //making the mouse leave its area close the menu
    @Override
    public void onMouseLeaveArea() {
        super.onMouseLeaveArea();
        if (!this.creatureNBT.nbtIsEmpty() && !this.isSelected) this.closeMenu(false);
    }

    private CreatureNBT getCreatureNBT() {
        if (this.section == SelectedCreatureInfo.SelectedPosType.PARTY) {
            return this.playerParty.getPartyMember(this.index);
        }
        else if (this.section == SelectedCreatureInfo.SelectedPosType.BOX) {
            return this.playerBox.getCreatureBoxStorage().getBoxContents(this.currentBoxIndexDynamic.getIntValue()).get(this.index);
        }
        else if (this.section == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
            return this.boxDeployedCreatures.getValue().get(this.index);
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
            new Rectangle().color(this.getInnerOutlineColor()).cornerRadius(5).draw(context, 1, 1, this.getArea().w() - 2, this.getArea().h() - 2, theme);

            //set background
            new Rectangle().color(this.getBackgroundColor()).cornerRadius(5).draw(context, 2, 2, this.getArea().w() - 4, this.getArea().h() - 4, theme);

            //set icon
            int iconSize = (int) (this.getArea().w() * 0.75);
            int iconOffset = (int) ((this.getArea().w() - iconSize) / 2D);
            RiftUIIcons.creatureIcon(this.creatureNBT.getCreatureType()).draw(context, iconOffset, iconOffset, iconSize, iconSize, theme);
        }
        else new Rectangle().color(0xFF212121).cornerRadius(5).drawAtZero(context, this.getArea(), theme);
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFallback();
    }

    private int getOutlineColor() {
        if (this.isSelected) return 0xFFFFFF00;
        else if (this.isHovering()) return 0xFFFFFFFF;
        return 0xFF000000;
    }

    private int getInnerOutlineColor() {
        if (this.creatureNBT.getCreatureHealth()[0] <= 0) return UIColors.creatureInlineDeadColor;
        else if (this.creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
            return UIColors.creatureInlineDeployedColor;
        }
        return UIColors.creatureInlineColor;
    }

    private int getBackgroundColor() {
        if (this.creatureNBT.getCreatureHealth()[0] <= 0) return UIColors.creatureBGDeadColor;
        else if (this.creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
            return UIColors.creatureBGDeployedColor;
        }
        return UIColors.creatureBGColor;
    }

    private static class MenuForCreature extends Menu<MenuForCreature> {
        private final CreatureInBoxButtonWidget parent;
        private final ObjectValue.Dynamic<CreatureNBT> parentCreatureNBT;
        private Boolean parentIsSelected;

        public MenuForCreature(CreatureInBoxButtonWidget parent) {
            super();
            this.parent = parent;
            this.parentCreatureNBT = new ObjectValue.Dynamic<>(
                    CreatureNBT.class,
                    () -> parent.creatureNBT,
                    null
            );
            this.coverChildrenHeight();
            this.unselectedFlex();
        }

        @Override
        public void onUpdate() {
            super.onUpdate();

            if (this.parentIsSelected == null || this.parentIsSelected != this.parent.isSelected) {
                //reset children
                this.removeAll();

                //selected means its a typical dropdown button
                if (this.parent.isSelected) {
                    this.width(64);

                    //set dropdown buttons
                    ListWidget<IWidget, ?> list = new ListWidget<>().widthRel(1f);
                    list.name("options").width(64).coverChildrenHeight().children(
                            Arrays.asList(Option.values()), option -> new CreatureDropdownOptionWidget(this.parent, option)
                    );
                    this.child(list);

                    //change flex to under the button
                    this.selectedFlex();
                }
                //otherwise, it just a panel that shows name, level, hp, energy, and health
                else {
                    this.width(96);

                    this.child(Flow.column().widthRel(1f).coverChildrenHeight().padding(3)
                            .childPadding(3)
                            //name
                            .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight()
                                    .child(IKey.dynamic(() -> {
                                        CreatureNBT cNBT = this.parentCreatureNBT.getValue();
                                        return cNBT.getCreatureName(false);
                                    }).scale(0.75f).asWidget())
                            )
                            //level
                            .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight()
                                    .child(IKey.dynamic(() -> {
                                        CreatureNBT cNBT = this.parentCreatureNBT.getValue();
                                        return I18n.format("tametrait.level", cNBT.getCreatureLevel());
                                    }).scale(0.75f).asWidget())
                            )
                            //health
                            .child(new RectangleProgressWidget().height(3).widthRel(1f)
                                    .valueColor(UIColors.barHealthColor)
                                    .setValue(new DoubleSyncValue(
                                            () -> this.parentCreatureNBT.getValue().getCreatureHealth()[0] / this.parentCreatureNBT.getValue().getCreatureHealth()[1],
                                            value -> {}
                                    ))
                            )
                            //energy
                            .child(new RectangleProgressWidget().height(3).widthRel(1f)
                                    .valueColor(UIColors.barEnergyColor)
                                    .setValue(new DoubleSyncValue(
                                            () -> (double) this.parentCreatureNBT.getValue().getCreatureEnergy()[0] / (double) this.parentCreatureNBT.getValue().getCreatureEnergy()[1],
                                            value -> {}
                                    ))
                            )
                            //xp
                            .child(new RectangleProgressWidget().height(3).widthRel(1f)
                                    .valueColor(UIColors.barXpColor)
                                    .setValue(new DoubleSyncValue(
                                            () -> (double) this.parentCreatureNBT.getValue().getCreatureXP()[0] / (double) this.parentCreatureNBT.getValue().getCreatureXP()[1],
                                            value -> {}
                                    ))
                            )
                    );

                    //change flex to the left or right of the button
                    this.unselectedFlex();
                }

                //set flag
                this.parentIsSelected = this.parent.isSelected;
            }
        }

        private void unselectedFlex() {
            //if on party or box, show to the right
            if (this.parent.section == SelectedCreatureInfo.SelectedPosType.PARTY
                || this.parent.section == SelectedCreatureInfo.SelectedPosType.BOX) {
                this.resizer().leftRel(1f).topRel(0.5f);
            }
            //otherwise, to the left
            else this.resizer().rightRel(1f).topRel(0.5f);
        }

        private void selectedFlex() {
            this.resizer().center().topRel(1f);
        }
    }

    private enum Option {
        INVENTORY,
        OPTIONS,
        INFO,
        MOVES;

        private String getTranslatedName() {
            return I18n.format("box.dropdown."+this.name().toLowerCase());
        }
    }

    private static class CreatureDropdownOptionWidget extends ButtonWidget<CreatureDropdownOptionWidget> {
        private final CreatureInBoxButtonWidget parent;
        private final Option option;

        public CreatureDropdownOptionWidget(CreatureInBoxButtonWidget parent, Option option) {
            this.parent = parent;
            this.option = option;
            this.name("Dropdown");
            this.widthRel(1f).height(10);
        }

        @Override
        public IDrawable getOverlay() {
            if (this.parent == null || this.parent.creatureNBT.nbtIsEmpty()) return IKey.NONE;
            String text = this.option.getTranslatedName();
            int textColor = this.isHovering() ? 0xFFFFFFFF : IKey.TEXT_COLOR;
            return IKey.str(text).scale(0.5f).color(textColor);
        }
    }
}
