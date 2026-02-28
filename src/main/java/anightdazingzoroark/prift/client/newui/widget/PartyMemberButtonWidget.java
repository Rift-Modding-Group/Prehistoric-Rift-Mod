package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.newui.UIColors;
import anightdazingzoroark.prift.client.newui.UIPanelNames;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.newui.screens.synced.RiftCreatureScreen;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.CapabilitySyncDirection;
import anightdazingzoroark.prift.server.capabilities.playerParty.IPlayerParty;
import anightdazingzoroark.prift.server.capabilities.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftOpenCreatureScreen;
import anightdazingzoroark.prift.server.message.RiftSyncPlayerParty;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.DrawableStack;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
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
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class PartyMemberButtonWidget extends ContextMenuButton<PartyMemberButtonWidget> implements Interactable {
    private final int index;
    private final EntityPlayer player;
    private final ObjectValue.Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic;
    private final BoolValue.Dynamic creatureSwitchingDynamic;
    private final SelectedCreatureInfo selectedCreatureInfo;
    private final IPlayerParty playerParty;

    private boolean syncFromServerFlag = true;
    private boolean syncToServerFlag;
    private boolean changeCreatureNBTFlag;
    private boolean isSelected;
    private boolean isSwitching;
    @NotNull
    private CreatureNBT creatureNBT = new CreatureNBT();

    public PartyMemberButtonWidget(int indexIn, EntityPlayer player, ObjectValue.Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic, BoolValue.Dynamic creatureSwitchingDynamic) {
        super(UIPanelNames.PARTY_DROPDOWN+indexIn);
        this.index = indexIn;
        this.player = player;
        this.creatureSwapInfoDynamic = creatureSwapInfoDynamic;
        this.creatureSwitchingDynamic = creatureSwitchingDynamic;
        this.selectedCreatureInfo = new SelectedCreatureInfo(SelectedCreatureInfo.SelectedPosType.PARTY, new int[]{indexIn});
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

        //-----syncing operations-----
        //sync from server only happens once and never again
        if (this.syncFromServerFlag) {
            PlayerPartyHelper.syncPlayerParty(this.player, CapabilitySyncDirection.SERVER_TO_CLIENT);
            this.creatureNBT = this.playerParty.getPartyMember(this.index);
            this.syncFromServerFlag = false;
        }

        //change creature nbt without syncing from server
        if (this.changeCreatureNBTFlag) {
            this.creatureNBT = this.playerParty.getPartyMember(this.index);
            this.changeCreatureNBTFlag = false;
        }

        //sync to server constantly happens
        if (this.syncToServerFlag) {
            PlayerPartyHelper.syncPlayerParty(this.player, CapabilitySyncDirection.CLIENT_TO_SERVER);
            this.syncToServerFlag = false;
        }

        //-----changes based on activation of switching mode-----
        if (this.creatureSwitchingDynamic.getBoolValue() != this.isSwitching) {
            this.isSwitching = this.creatureSwitchingDynamic.getBoolValue();

            //reset swap info
            this.creatureSwapInfoDynamic.getValue().clear();

            //resetting isSelected based on if isCreatureSwitching just got changed
            this.isSelected = false;

            //close dropdowns
            if (this.getMenu().isValid()) this.getMenu().getPanel().closeIfOpen();
        }
    }

    @Override
    @NotNull
    public Result onMousePressed(int mouseButton) {
        //only continue if nbt is not empty, or if nbt is empty but switching is enabled
        boolean canUseCondition = !this.creatureNBT.nbtIsEmpty()
                || (this.creatureNBT.nbtIsEmpty() && this.creatureSwitchingDynamic.getBoolValue() && this.creatureSwapInfoDynamic.getValue().canSwapHalfway());
        if (!canUseCondition) return Result.ACCEPT;

        Interactable.playButtonClickSound();
        this.isSelected = !this.isSelected;
        if (!(this.getParent() instanceof PaddedGrid gridParent)) return super.onMousePressed(mouseButton);

        //clear other already opened dropdowns and their selected flags
        for (IWidget child : gridParent.getChildren()) {
            if (!(child instanceof PartyMemberButtonWidget partyMemButton)) continue;
            if (!partyMemButton.getMenu().isValid()) continue;
            if (partyMemButton.index != this.index) partyMemButton.isSelected = false;
            partyMemButton.getMenu().getPanel().closeIfOpen();
        }

        //when not swapping, just return default value
        if (!this.creatureSwitchingDynamic.getBoolValue()) return super.onMousePressed(mouseButton);

        //otherwise, regular swapping operations
        if (!this.creatureSwapInfoDynamic.getValue().canSwap()) {
            this.creatureSwapInfoDynamic.getValue().setCreature(this.selectedCreatureInfo);
        }
        if (this.creatureSwapInfoDynamic.getValue().canSwap()) {
            this.syncToServerFlag = this.creatureSwapInfoDynamic.getValue().applySwap(this.playerParty);

            //apply change to all buttons
            for (IWidget child : gridParent.getChildren()) {
                if (!(child instanceof PartyMemberButtonWidget partyMemButton)) continue;
                //partyMemButton.syncFromServerFlag = true;
                partyMemButton.changeCreatureNBTFlag = true;
                partyMemButton.isSelected = false;
            }
        }
        return Result.SUCCESS;
    }

    public Menu<?> getButtonMenu() {
        if (!this.getMenu().isValid()) return null;
        return this.getMenu();
    }

    public void closeButtonMenu() {
        this.isSelected = false;
        this.closeMenu(false);
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
            Icon creatureIcon = UITexture.fullImage(
                    new ResourceLocation(
                            RiftInitialize.MODID,
                            "textures/icons/"+this.creatureNBT.getCreatureType().toString().toLowerCase()+"_icon.png"
                    )
            ).asIcon();
            creatureIcon.draw(context, 4, 4, 24, 24, theme);

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
    public PartyMemberButtonWidget menuList(Consumer<ListWidget<IWidget, ?>> builder) {
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
        else if (this.creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) return 0xFF208620;
        return 0xFFC6C6C6;
    }

    private enum Option {
        INVENTORY(
                (index, playerParty) -> true,
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getParty().get(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);
                    if (creature != null) {
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, creature, RiftCreatureScreen.inventoryPageNum));
                    }
                    else {
                        SelectedCreatureInfo selectionInfo = new SelectedCreatureInfo(
                                SelectedCreatureInfo.SelectedPosType.PARTY,
                                new int[]{index},
                                SelectedCreatureInfo.MenuOpenedFrom.PARTY
                        );
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, selectionInfo, RiftCreatureScreen.inventoryPageNum));
                    }
                    return true;
                }
        ),
        OPTIONS(
                (index, playerParty) -> true,
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getParty().get(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);
                    if (creature != null) {
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, creature, RiftCreatureScreen.optionsPageNum));
                    }
                    else {
                        SelectedCreatureInfo selectionInfo = new SelectedCreatureInfo(
                                SelectedCreatureInfo.SelectedPosType.PARTY,
                                new int[]{index},
                                SelectedCreatureInfo.MenuOpenedFrom.PARTY
                        );
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, selectionInfo, RiftCreatureScreen.optionsPageNum));
                    }
                    return true;
                }
        ),
        INFO(
                (index, playerParty) -> true,
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getParty().get(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);
                    if (creature != null) {
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, creature, RiftCreatureScreen.infoPageNum));
                    }
                    else {
                        SelectedCreatureInfo selectionInfo = new SelectedCreatureInfo(
                                SelectedCreatureInfo.SelectedPosType.PARTY,
                                new int[]{index},
                                SelectedCreatureInfo.MenuOpenedFrom.PARTY
                        );
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, selectionInfo, RiftCreatureScreen.infoPageNum));
                    }
                    return true;
                }
        ),
        MOVES(
                (index, playerParty) -> true,
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getParty().get(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);
                    if (creature != null) {
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, creature, RiftCreatureScreen.movesPageNum));
                    }
                    else {
                        SelectedCreatureInfo selectionInfo = new SelectedCreatureInfo(
                                SelectedCreatureInfo.SelectedPosType.PARTY,
                                new int[]{index},
                                SelectedCreatureInfo.MenuOpenedFrom.PARTY
                        );
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, selectionInfo, RiftCreatureScreen.movesPageNum));
                    }
                    return true;
                }
        ),
        SUMMON_OR_DISMISS(
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getParty().get(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);

                    //if creature is found, almost anything may be done with it
                    if (creature != null) return true;
                    else {
                        //dont summon when creature is dead
                        if (creatureNBT.getCreatureHealth()[0] <= 0) return false;
                            //dont summon when player not in apt position
                        else if (!playerParty.canDeployPartyMember(index, player)) return false;
                            //final return value
                        else return true;
                    }
                },
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getParty().get(index);
                    boolean deploymentToggle = creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE;

                    playerParty.deployPartyMember(index, deploymentToggle, player);
                    return true;
                },
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getParty().get(index);

                    if (creatureNBT.getCreatureHealth()[0] <= 0) return I18n.format("party.warning.cannot_summon_dead");
                    else if (!playerParty.canDeployPartyMember(index, player)) return I18n.format("party.warning.cannot_summon");
                    return "";
                }
        ),
        TELEPORT(
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getParty().get(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);

                    //return value is based on if creature exists in the world or not
                    return creature != null;
                },
                (index, playerParty) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerParty.getParty().get(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);

                    if (creature != null) {
                        //check first if the creature can be teleported to that spot first
                        if (playerParty.canDeployPartyMember(index, player)) {
                            playerParty.teleportPartyMember(index, player);
                            return true;
                        }
                    }
                    return false;
                },
                (index, playerParty) -> I18n.format("party.warning.cannot_teleport")
        );

        private final BiFunction<Integer, IPlayerParty, Boolean> canBeClicked;
        private final BiFunction<Integer, IPlayerParty, Boolean> clickResult;
        private final BiFunction<Integer, IPlayerParty, String> ineligibilityText;

        Option(BiFunction<Integer, IPlayerParty, Boolean> canBeClicked, BiFunction<Integer, IPlayerParty, Boolean> clickResult) {
            this(canBeClicked, clickResult, null);
        }

        Option(BiFunction<Integer, IPlayerParty, Boolean> canBeClicked,
               BiFunction<Integer, IPlayerParty, Boolean> clickResult,
               BiFunction<Integer, IPlayerParty, String> ineligibilityText) {
            this.canBeClicked = canBeClicked;
            this.clickResult = clickResult;
            this.ineligibilityText = ineligibilityText;
        }

        public boolean canBeClicked(int index, IPlayerParty playerParty) {
            return this.canBeClicked.apply(index, playerParty);
        }

        public boolean click(int index, IPlayerParty playerParty) {
            return this.clickResult.apply(index, playerParty);
        }

        public String getIneligibilityText(int index, IPlayerParty playerParty) {
            if (this.ineligibilityText == null) return "";
            return this.ineligibilityText.apply(index, playerParty);
        }

        public boolean hasIneligibilityText() {
            return this.ineligibilityText != null;
        }

        public String getTranslatedName(int index, IPlayerParty playerParty) {
            CreatureNBT creatureNBT = playerParty.getPartyMember(index);
            String strikethrough = !this.canBeClicked.apply(index, playerParty) ? IKey.STRIKETHROUGH.toString() : "";
            if (this != SUMMON_OR_DISMISS) return strikethrough+I18n.format("party.dropdown."+this.name().toLowerCase());
            if (creatureNBT == null || creatureNBT.nbtIsEmpty()) return "";
            if (creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                return strikethrough+I18n.format("party.dropdown.dismiss");
            }
            else if (creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                return strikethrough+I18n.format("party.dropdown.summon");
            }
            return "";
        }
    }

    private static class PartyMemberDropdownOptionWidget extends ButtonWidget<PartyMemberDropdownOptionWidget> {
        private final PartyMemberButtonWidget parent;
        private final PartyMemberButtonWidget.Option option;

        public PartyMemberDropdownOptionWidget(@NotNull PartyMemberButtonWidget parent, PartyMemberButtonWidget.Option optionIn) {
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
                this.parent.changeCreatureNBTFlag = true;
                this.parent.syncToServerFlag = true;
                this.markAllTooltipsDirty();
                return Result.SUCCESS;
            }
            return Result.ACCEPT;
        }

        private void markAllTooltipsDirty() {
            if (!(this.getParent() instanceof ListWidget<?,?> listWidgetParent)) return;
            for (IWidget child : listWidgetParent.getChildren()) {
                if (!(child instanceof PartyMemberButtonWidget.PartyMemberDropdownOptionWidget optionWidget)) continue;
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
