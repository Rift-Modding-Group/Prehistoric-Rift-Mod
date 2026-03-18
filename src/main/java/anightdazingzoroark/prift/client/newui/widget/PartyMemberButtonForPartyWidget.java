package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.client.newui.RiftUIIcons;
import anightdazingzoroark.prift.client.newui.UIColors;
import anightdazingzoroark.prift.client.newui.UIPanelNames;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.newui.screens.synced.RiftCreatureScreen;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.entity.CreatureDeployment;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftOpenCreatureScreen;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
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
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.ObjectValue;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.menu.ContextMenuButton;
import com.cleanroommc.modularui.widgets.menu.Menu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class PartyMemberButtonForPartyWidget extends ContextMenuButton<PartyMemberButtonForPartyWidget> implements Interactable {
    private final int index;
    private final ObjectValue.Dynamic<SelectedCreatureInfo> selectedCreatureInfoDynamic;
    private final ObjectValue.Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic;
    private final BoolValue.Dynamic creatureSwitchingDynamic;
    private final SelectedCreatureInfo selectedCreatureInfo;
    private final PlayerPartyProperties playerParty;

    private boolean isSelected;
    @NotNull
    private CreatureNBT creatureNBT = new CreatureNBT();

    public PartyMemberButtonForPartyWidget(
            int indexIn,
            EntityPlayer player,
            ObjectValue.Dynamic<SelectedCreatureInfo> selectedCreatureInfoDynamic,
            ObjectValue.Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic,
            BoolValue.Dynamic creatureSwitchingDynamic
    ) {
        super(UIPanelNames.PARTY_DROPDOWN+indexIn);
        this.index = indexIn;
        this.selectedCreatureInfoDynamic = selectedCreatureInfoDynamic;
        this.creatureSwapInfoDynamic = creatureSwapInfoDynamic;
        this.creatureSwitchingDynamic = creatureSwitchingDynamic;
        this.selectedCreatureInfo = SelectedCreatureInfo.partySelectedInfo(indexIn);
        this.playerParty = PlayerPartyHelper.getPlayerParty(player);

        this.requiresClick();
        this.size(80, 48);
        this.openDown();
        this.menuList(list -> {
            list.name("options").width(64).coverChildrenHeight().children(
                    Arrays.asList(Option.values()), option -> new PartyMemberDropdownOptionWidget(this, option)
            );
        });
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.isValid()) return;

        //update creature
        this.creatureNBT = this.getCreatureNBT();
        this.isSelected = this.selectedCreatureInfo.equals(this.selectedCreatureInfoDynamic.getValue());
    }

    //get the nbt. its info will be displayed, and if its deployed, should update dynamically
    private CreatureNBT getCreatureNBT() {
        //to dynamically update deployed creature info
        if (this.playerParty.deployedCreatureIsLoadedAtIndex(this.index)) {
            return new CreatureNBT(this.playerParty.getLoadedDeployedCreature(this.index));
        }
        //if its not deployed, it remains as is
        return this.playerParty.getPartyMember(this.index);
    }

    //wrapper for use in public for closing this button's menu if its ever open
    public void closeMenu() {
        if (this.getMenu().isValid()) this.getMenu().getPanel().closeIfOpen();
    }

    @Override
    @NotNull
    public Result onMousePressed(int mouseButton) {
        //only continue if nbt is not empty, or if nbt is empty but switching is enabled
        boolean canUseCondition = !this.creatureNBT.nbtIsEmpty()
                || (this.creatureNBT.nbtIsEmpty() && this.creatureSwitchingDynamic.getBoolValue() && this.creatureSwapInfoDynamic.getValue().canSwapHalfway());
        if (!canUseCondition) return Result.ACCEPT;

        Interactable.playButtonClickSound();
        this.selectedCreatureInfoDynamic.setValue(this.selectedCreatureInfo);
        if (!(this.getParent() instanceof PaddedGrid gridParent)) return super.onMousePressed(mouseButton);

        //clear other already opened dropdowns
        for (IWidget child : gridParent.getChildren()) {
            if (!(child instanceof PartyMemberButtonForPartyWidget partyMemButton)) continue;
            if (!partyMemButton.getMenu().isValid()) continue;
            partyMemButton.getMenu().getPanel().closeIfOpen();
        }

        //when not swapping, just return default value
        if (!this.creatureSwitchingDynamic.getBoolValue()) return super.onMousePressed(mouseButton);

        //otherwise, regular swapping operations
        if (!this.creatureSwapInfoDynamic.getValue().canSwap()) {
            this.creatureSwapInfoDynamic.getValue().setCreature(this.selectedCreatureInfo);
        }
        return Result.SUCCESS;
    }

    public Menu<?> getButtonMenu() {
        if (!this.getMenu().isValid()) return null;
        return this.getMenu();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());
        float textScale = 0.5f;

        if (!this.creatureNBT.nbtIsEmpty()) {
            //draw border, color changes depending on whether or not its hovered
            new Rectangle().color(this.getOutlineColor()).cornerRadius(5).drawAtZero(context, this.getArea(), theme);

            //draw background
            Area bgArea = new Area(this.getArea());
            bgArea.h(bgArea.h() - 2);
            bgArea.w(bgArea.w() - 2);
            new Rectangle().color(0xFFC6C6C6).cornerRadius(5).draw(context, 1, 1, bgArea.w(), bgArea.h(), theme);

            //draw container for creature icon
            new Rectangle().color(this.getOutlineColor()).cornerRadius(5).draw(context, 2, 2, 28, 28, theme);
            new Rectangle().color(this.getIconBGColor()).cornerRadius(5).draw(context, 3, 3, 26, 26, theme);

            //draw creature icon
            RiftUIIcons.creatureIcon(this.creatureNBT.getCreatureType()).draw(context, 4, 4, 24, 24, theme);

            //define strings and other values
            IKey creatureNameString = IKey.str(this.creatureNBT.getCreatureName(false)).alignment(Alignment.CenterLeft);
            IKey creatureLevelString = IKey.lang("tametrait.level", this.creatureNBT.getCreatureLevel()).alignment(Alignment.CenterLeft);
            int longestWidth = RiftUtil.maxWithinMultiple(
                    creatureNameString.asTextIcon().getWidth(),
                    creatureLevelString.asTextIcon().getWidth()
            );
            int xTextOff = 64;

            //draw creature name
            creatureNameString.scale(textScale)
                    .color(theme.getTextColor())
                    .draw(context, (int) (xTextOff * textScale), (int) (8 * textScale),
                            longestWidth,
                            creatureNameString.asTextIcon().getHeight(),
                            theme
                    );

            //draw creature level
            creatureLevelString.scale(textScale)
                    .color(theme.getTextColor())
                    .draw(context, (int) (xTextOff * textScale), (int) (24 * textScale),
                            longestWidth,
                            creatureLevelString.asTextIcon().getHeight(),
                            theme
                    );

            //bar initialization
            int barWidth = this.getArea().w() - 8;

            //-----health bar-----
            //background
            new Rectangle().color(UIColors.barBorderColor).draw(context, 3, 32, this.getArea().w() - 6, 3, theme);
            new Rectangle().color(UIColors.barEmptyColor).draw(context, 4, 33, this.getArea().w() - 8, 1, theme);

            //health
            float healthPercentage = this.creatureNBT.getCreatureHealth()[0] / this.creatureNBT.getCreatureHealth()[1];
            int healthBar = (int) (healthPercentage * barWidth);
            new Rectangle().color(UIColors.barHealthColor).draw(context, 4, 33, healthBar, 1, theme);

            //-----energy bar-----
            //background
            new Rectangle().color(UIColors.barBorderColor).draw(context, 3, 36, this.getArea().w() - 6, 3, theme);
            new Rectangle().color(UIColors.barEmptyColor).draw(context, 4, 37, this.getArea().w() - 8, 1, theme);

            //energy
            float energyPercentage = (float) this.creatureNBT.getCreatureEnergy()[0] / this.creatureNBT.getCreatureEnergy()[1];
            int energyBar = (int) (energyPercentage * barWidth);
            new Rectangle().color(UIColors.barEnergyColor).draw(context, 4, 37, energyBar, 1, theme);

            //-----xp bar-----
            //background
            new Rectangle().color(UIColors.barBorderColor).draw(context, 3, 40, this.getArea().w() - 6, 3, theme);
            new Rectangle().color(UIColors.barEmptyColor).draw(context, 4, 41, this.getArea().w() - 8, 1, theme);

            //xp
            float xpPercentage = (float) this.creatureNBT.getCreatureXP()[0] / this.creatureNBT.getCreatureXP()[1];
            int xpBar = (int) (xpPercentage * barWidth);
            new Rectangle().color(UIColors.barXpColor).draw(context, 4, 41, xpBar, 1, theme);
        }
        else {
            //to give the illusion that there's a border since rectangles w corner radiuses cant be hollow
            //we doin this trick where the border is same color as contents until it hovered and when we doin
            //swappin
            boolean canHaveBorder = this.creatureNBT.nbtIsEmpty() && this.creatureSwitchingDynamic.getBoolValue();
            int hoveredColor = (canHaveBorder && this.isHovering()) ? 0xFFFFFFFF : 0xFF212121;
            new Rectangle().color(hoveredColor).cornerRadius(5).drawAtZero(context, this.getArea(), theme);

            //le empty box
            Area bgArea = new Area(this.getArea());
            bgArea.h(bgArea.h() - 2);
            bgArea.w(bgArea.w() - 2);
            new Rectangle().color(0xFF212121).cornerRadius(5).draw(context, 1, 1, bgArea.w(), bgArea.h(), theme);
        }
    }

    @Override
    public PartyMemberButtonForPartyWidget menuList(Consumer<ListWidget<IWidget, ?>> builder) {
        ListWidget<IWidget, ?> l = new ListWidget<>().widthRel(1f);
        builder.accept(l);
        return this.menu(new Menu<>().width(64).center()
                .coverChildrenHeight().child(l)
        );
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

    private int getIconBGColor() {
        if (this.creatureNBT.nbtIsEmpty()) return -1;
        else if (this.creatureNBT.getCreatureHealth()[0] <= 0) return 0xFFF33F3F;
        else if (this.creatureNBT.getDeploymentType() == CreatureDeployment.PARTY) return 0xFF208620;
        return 0xFFC6C6C6;
    }

    private enum Option {
        INVENTORY(
                (index, playerParty) -> true,
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getPartyMember(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);
                    if (creature != null) {
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, creature, RiftCreatureScreen.inventoryPageNum));
                    }
                    else {
                        SelectedCreatureInfo selectionInfo = SelectedCreatureInfo.partySelectedInfo(index);
                        selectionInfo.setMenuOpenedFrom(SelectedCreatureInfo.MenuOpenedFrom.PARTY);
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, selectionInfo, RiftCreatureScreen.inventoryPageNum));
                    }
                    return true;
                }
        ),
        OPTIONS(
                (index, playerParty) -> true,
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getPartyMember(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);
                    if (creature != null) {
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, creature, RiftCreatureScreen.optionsPageNum));
                    }
                    else {
                        SelectedCreatureInfo selectionInfo = SelectedCreatureInfo.partySelectedInfo(index);
                        selectionInfo.setMenuOpenedFrom(SelectedCreatureInfo.MenuOpenedFrom.PARTY);
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, selectionInfo, RiftCreatureScreen.optionsPageNum));
                    }
                    return true;
                }
        ),
        INFO(
                (index, playerParty) -> true,
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getPartyMember(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);
                    if (creature != null) {
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, creature, RiftCreatureScreen.infoPageNum));
                    }
                    else {
                        SelectedCreatureInfo selectionInfo = SelectedCreatureInfo.partySelectedInfo(index);
                        selectionInfo.setMenuOpenedFrom(SelectedCreatureInfo.MenuOpenedFrom.PARTY);
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, selectionInfo, RiftCreatureScreen.infoPageNum));
                    }
                    return true;
                }
        ),
        MOVES(
                (index, playerParty) -> true,
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getPartyMember(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);
                    if (creature != null) {
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, creature, RiftCreatureScreen.movesPageNum));
                    }
                    else {
                        SelectedCreatureInfo selectionInfo = SelectedCreatureInfo.partySelectedInfo(index);
                        selectionInfo.setMenuOpenedFrom(SelectedCreatureInfo.MenuOpenedFrom.PARTY);
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, selectionInfo, RiftCreatureScreen.movesPageNum));
                    }
                    return true;
                }
        ),
        SUMMON_OR_DISMISS(
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getPartyMember(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);

                    //if creature is found, almost anything may be done with it
                    if (creature != null) return true;
                    else {
                        //dont summon when creature is dead
                        if (creatureNBT.getCreatureHealth()[0] <= 0) return false;
                            //dont summon when player not in apt position
                        else if (!playerParty.canDeployPartyMember(index)) return false;
                            //final return value
                        else return true;
                    }
                },
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getPartyMember(index);
                    boolean deploymentToggle = creatureNBT.getDeploymentType() == CreatureDeployment.PARTY_INACTIVE;
                    PlayerPartyHelper.deployCreatureClient(player, index, deploymentToggle);
                    return true;
                },
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getPartyMember(index);

                    if (creatureNBT.getCreatureHealth()[0] <= 0) return I18n.format("party.warning.cannot_summon_dead");
                    else if (!playerParty.canDeployPartyMember(index)) return I18n.format("party.warning.cannot_summon");
                    return "";
                }
        ),
        TELEPORT(
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getPartyMember(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);

                    //return value is based on if creature exists in the world or not
                    return creature != null;
                },
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getPartyMember(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);

                    if (creature != null) {
                        //check first if the creature can be teleported to that spot first
                        if (playerParty.canDeployPartyMember(index)) {
                            PlayerPartyHelper.teleportCreatureClient(player, index);
                            return true;
                        }
                    }
                    return false;
                },
                (index, playerParty) -> I18n.format("party.warning.cannot_teleport")
        );

        private final BiFunction<Integer, PlayerPartyProperties, Boolean> canBeClicked;
        private final BiFunction<Integer, PlayerPartyProperties, Boolean> clickResult;
        private final BiFunction<Integer, PlayerPartyProperties, String> ineligibilityText;

        Option(BiFunction<Integer, PlayerPartyProperties, Boolean> canBeClicked, BiFunction<Integer, PlayerPartyProperties, Boolean> clickResult) {
            this(canBeClicked, clickResult, null);
        }

        Option(BiFunction<Integer, PlayerPartyProperties, Boolean> canBeClicked,
               BiFunction<Integer, PlayerPartyProperties, Boolean> clickResult,
               BiFunction<Integer, PlayerPartyProperties, String> ineligibilityText) {
            this.canBeClicked = canBeClicked;
            this.clickResult = clickResult;
            this.ineligibilityText = ineligibilityText;
        }

        public boolean canBeClicked(int index, PlayerPartyProperties playerParty) {
            return this.canBeClicked.apply(index, playerParty);
        }

        public boolean click(int index, PlayerPartyProperties playerParty) {
            return this.clickResult.apply(index, playerParty);
        }

        public String getIneligibilityText(int index, PlayerPartyProperties playerParty) {
            if (this.ineligibilityText == null) return "";
            return this.ineligibilityText.apply(index, playerParty);
        }

        public boolean hasIneligibilityText() {
            return this.ineligibilityText != null;
        }

        public String getTranslatedName(int index, PlayerPartyProperties playerParty) {
            CreatureNBT creatureNBT = playerParty.getPartyMember(index);
            String strikethrough = !this.canBeClicked.apply(index, playerParty) ? IKey.STRIKETHROUGH.toString() : "";
            if (this != SUMMON_OR_DISMISS) return strikethrough+I18n.format("party.dropdown."+this.name().toLowerCase());
            if (creatureNBT == null || creatureNBT.nbtIsEmpty()) return "";
            if (creatureNBT.getDeploymentType() == CreatureDeployment.PARTY) {
                return strikethrough+I18n.format("party.dropdown.dismiss");
            }
            else if (creatureNBT.getDeploymentType() == CreatureDeployment.PARTY_INACTIVE) {
                return strikethrough+I18n.format("party.dropdown.summon");
            }
            return "";
        }
    }

    private static class PartyMemberDropdownOptionWidget extends ButtonWidget<PartyMemberDropdownOptionWidget> {
        private final PartyMemberButtonForPartyWidget parent;
        private final PartyMemberButtonForPartyWidget.Option option;

        public PartyMemberDropdownOptionWidget(@NotNull PartyMemberButtonForPartyWidget parent, PartyMemberButtonForPartyWidget.Option optionIn) {
            this.parent = parent;
            this.option = optionIn;
            this.name("Dropdown");
            this.widthRel(1f).height(10);
            this.tooltipBuilder(tooltipBuilder -> {
                if (this.parent.creatureNBT.nbtIsEmpty()) return;
                if (this.option.hasIneligibilityText() && !this.option.canBeClicked(this.parent.index, this.parent.playerParty)) {
                    String result = this.option.getIneligibilityText(this.parent.index, this.parent.playerParty);
                    if (!result.isEmpty()) tooltipBuilder.addLine(result);
                }
            });
        }

        @Override
        @NotNull
        public Result onMousePressed(int mouseButton) {
            if (this.parent == null) return Result.ACCEPT;
            if (this.parent.creatureNBT.nbtIsEmpty()) return Result.ACCEPT;
            if (!this.option.canBeClicked(this.parent.index, this.parent.playerParty)) return Result.ACCEPT;
            if (this.option.click(this.parent.index, this.parent.playerParty)) {
                Interactable.playButtonClickSound();
                this.markAllTooltipsDirty();
                return Result.SUCCESS;
            }
            return Result.ACCEPT;
        }

        private void markAllTooltipsDirty() {
            if (!(this.getParent() instanceof ListWidget<?,?> listWidgetParent)) return;
            for (IWidget child : listWidgetParent.getChildren()) {
                if (!(child instanceof PartyMemberButtonForPartyWidget.PartyMemberDropdownOptionWidget optionWidget)) continue;
                optionWidget.markTooltipDirty();
            }
        }

        @Override
        public IDrawable getOverlay() {
            if (this.parent == null || this.parent.creatureNBT.nbtIsEmpty()) return IKey.NONE;
            String text = this.option.getTranslatedName(this.parent.index, this.parent.playerParty);
            int textColor = this.isHovering() ? 0xFFFFFFFF : IKey.TEXT_COLOR;
            return IKey.str(text).scale(0.5f).color(textColor);
        }

        @Override
        public IDrawable getHoverBackground() {
            if (this.parent == null || this.parent.creatureNBT.nbtIsEmpty()) return IKey.NONE;
            if (this.option.canBeClicked(this.parent.index, this.parent.playerParty)) {
                return new DrawableStack(
                        new Rectangle().color(0xFFB5377B),
                        new Rectangle().color(0xFF942C64).hollow(0.5f)
                );
            }
            return IKey.NONE;
        }
    }
}
