package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.newui.RiftCreatureScreen;
import anightdazingzoroark.prift.client.newui.UIColors;
import anightdazingzoroark.prift.client.newui.UIPanelNames;
import anightdazingzoroark.prift.client.newui.sync.CreatureSwapInfoSyncValue;
import anightdazingzoroark.prift.client.newui.sync.PlayerPartySyncValue;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftOpenCreatureScreen;
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
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
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
import java.util.function.*;

public class PartyMemberButtonWidget extends ContextMenuButton<PartyMemberButtonWidget> implements Interactable {
    private final int index;
    private final PlayerPartySyncValue playerParty;
    private final BooleanSyncValue isCreatureSwitching;

    private final CreatureSwapInfoSyncValue swapInfo;
    private final SelectedCreatureInfo selectedCreatureInfo;
    private boolean isSelected;
    private boolean isSwitching;
    private CreatureNBT creatureNBT;
    private boolean markForSync = true;

    public PartyMemberButtonWidget(int indexIn, PlayerPartySyncValue playerParty, BooleanSyncValue isCreatureSwitching, CreatureSwapInfoSyncValue swapInfo) {
        super(UIPanelNames.PARTY_DROPDOWN+indexIn);
        this.index = indexIn;
        this.playerParty = playerParty;
        this.isCreatureSwitching = isCreatureSwitching;
        this.swapInfo = swapInfo;
        this.selectedCreatureInfo = new SelectedCreatureInfo(SelectedCreatureInfo.SelectedPosType.PARTY, new int[]{indexIn});
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

        //setting nbt based on flags for syncing
        if (this.markForSync && this.playerParty.isClientSyncInitialized()) {
            this.creatureNBT = this.playerParty.getValue().getPartyMember(this.index);
            this.markForSync = false;
        }

        //changes based on activation of switching mode
        if (this.isCreatureSwitching.getBoolValue() != this.isSwitching) {
            this.isSwitching = this.isCreatureSwitching.getBoolValue();

            //resetting isSelected based on if isCreatureSwitching just got changed
            this.isSelected = false;

            //close dropdowns
            if (this.getMenu().isValid()) this.getMenu().getPanel().closeIfOpen();

        }
    }

    @Override
    @NotNull
    public Result onMousePressed(int mouseButton) {
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
        if (!this.isCreatureSwitching.getBoolValue()) return super.onMousePressed(mouseButton);

        //otherwise, regular swapping operations
        if (!this.swapInfo.getValue().canSwap()) {
            this.swapInfo.getValue().setCreature(this.selectedCreatureInfo);
        }
        if (this.swapInfo.getValue().canSwap()) {
            this.swapInfo.getValue().applySwap(this.playerParty);

            //apply change to all buttons
            for (IWidget child : gridParent.getChildren()) {
                if (!(child instanceof PartyMemberButtonWidget partyMemButton)) continue;
                partyMemButton.markForSync = true;
                partyMemButton.isSelected = false;
            }
        }
        return Result.SUCCESS;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());
        float textScale = 0.5f;

        if (this.creatureNBT != null && !this.creatureNBT.nbtIsEmpty()) {
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
        else new Rectangle().color(0xFF212121).cornerRadius(5).drawAtZero(context, this.getArea(), theme);
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
                (index, playerPartySyncValue) -> true,
                (index, playerPartySyncValue) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerPartySyncValue.getValue().getParty().get(index);
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
                (index, playerPartySyncValue) -> true,
                (index, playerPartySyncValue) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerPartySyncValue.getValue().getParty().get(index);
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
                (index, playerPartySyncValue) -> true,
                (index, playerPartySyncValue) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerPartySyncValue.getValue().getParty().get(index);
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
                (index, playerPartySyncValue) -> true,
                (index, playerPartySyncValue) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerPartySyncValue.getValue().getParty().get(index);
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
                (index, playerPartySyncValue) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerPartySyncValue.getValue().getParty().get(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);

                    //if creature is found, almost anything may be done with it
                    if (creature != null) return true;
                    else {
                        //dont summon when creature is dead
                        if (creatureNBT.getCreatureHealth()[0] <= 0) return false;
                        //dont summon when player not in apt position
                        else if (!playerPartySyncValue.getValue().canDeployPartyMember(index, player)) return false;
                        //final return value
                        else return true;
                    }
                },
                (index, playerPartySyncValue) -> {
                    CreatureNBT creatureNBT = playerPartySyncValue.getValue().getParty().get(index);
                    boolean deploymentToggle = creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE;

                    playerPartySyncValue.deployAtIndex(index, deploymentToggle);
                    return true;
                },
                (index, playerPartySyncValue) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerPartySyncValue.getValue().getParty().get(index);

                    if (creatureNBT.getCreatureHealth()[0] <= 0) return I18n.format("party.warning.cannot_summon_dead");
                    else if (!playerPartySyncValue.getValue().canDeployPartyMember(index, player)) return I18n.format("party.warning.cannot_summon");
                    return "";
                }
        ),
        TELEPORT(
                (index, playerPartySyncValue) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerPartySyncValue.getValue().getParty().get(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);

                    //return value is based on if creature exists in the world or not
                    return creature != null;
                },
                (index, playerPartySyncValue) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    CreatureNBT creatureNBT = playerPartySyncValue.getValue().getParty().get(index);
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(player.world);

                    if (creature != null) {
                        //check first if the creature can be teleported to that spot first
                        if (playerPartySyncValue.getValue().canDeployPartyMember(index, player)) {
                            playerPartySyncValue.teleportAtIndex(index);
                            return true;
                        }
                    }
                    return false;
                },
                (index, playerPartySyncValue) -> I18n.format("party.warning.cannot_teleport")
        );

        private final BiFunction<Integer, PlayerPartySyncValue, Boolean> canBeClicked;
        private final BiFunction<Integer, PlayerPartySyncValue, Boolean> clickResult;
        private final BiFunction<Integer, PlayerPartySyncValue, String> ineligibilityText;

        Option(BiFunction<Integer, PlayerPartySyncValue, Boolean> canBeClicked, BiFunction<Integer, PlayerPartySyncValue, Boolean> clickResult) {
            this(canBeClicked, clickResult, null);
        }

        Option(BiFunction<Integer, PlayerPartySyncValue, Boolean> canBeClicked,
               BiFunction<Integer, PlayerPartySyncValue, Boolean> clickResult,
               BiFunction<Integer, PlayerPartySyncValue, String> ineligibilityText) {
            this.canBeClicked = canBeClicked;
            this.clickResult = clickResult;
            this.ineligibilityText = ineligibilityText;
        }

        public boolean canBeClicked(int index, PlayerPartySyncValue playerPartySyncValue) {
            return this.canBeClicked.apply(index, playerPartySyncValue);
        }

        public boolean click(int index, PlayerPartySyncValue playerPartySyncValue) {
            return this.clickResult.apply(index, playerPartySyncValue);
        }

        public String getIneligibilityText(int index, PlayerPartySyncValue playerPartySyncValue) {
            if (this.ineligibilityText == null) return "";
            return this.ineligibilityText.apply(index, playerPartySyncValue);
        }

        public boolean hasIneligibilityText() {
            return this.ineligibilityText != null;
        }

        public String getTranslatedName(int index, PlayerPartySyncValue playerPartySyncValue) {
            CreatureNBT creatureNBT = playerPartySyncValue.getValue().getPartyMember(index);
            String strikethrough = !this.canBeClicked.apply(index, playerPartySyncValue) ? IKey.STRIKETHROUGH.toString() : "";
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
        private final Option option;

        public PartyMemberDropdownOptionWidget(@NotNull PartyMemberButtonWidget parent, Option optionIn) {
            this.parent = parent;
            this.option = optionIn;
            this.name("Dropdown");
            this.widthRel(1f).height(10);
            this.tooltipBuilder(tooltipBuilder -> {
                if (this.parent.creatureNBT == null || this.parent.creatureNBT.nbtIsEmpty()) return;
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
            if (this.parent.creatureNBT == null || this.parent.creatureNBT.nbtIsEmpty()) return Result.ACCEPT;
            if (!this.option.canBeClicked(this.parent.index, this.parent.playerParty)) return Result.ACCEPT;
            if (this.option.click(this.parent.index, this.parent.playerParty)) {
                Interactable.playButtonClickSound();
                this.parent.markForSync = true;
                this.markAllTooltipsDirty();
                return Result.SUCCESS;
            }
            return Result.ACCEPT;
        }

        private void markAllTooltipsDirty() {
            if (!(this.getParent() instanceof ListWidget<?,?> listWidgetParent)) return;
            for (IWidget child : listWidgetParent.getChildren()) {
                if (!(child instanceof PartyMemberDropdownOptionWidget optionWidget)) continue;
                optionWidget.markTooltipDirty();
            }
        }

        @Override
        public IDrawable getOverlay() {
            if (this.parent == null || this.parent.creatureNBT == null || this.parent.creatureNBT.nbtIsEmpty()) return IKey.NONE;
            String text = this.option.getTranslatedName(this.parent.index, this.parent.playerParty);
            int textColor = this.isHovering() ? 0xFFFFFFFF : IKey.TEXT_COLOR;
            return IKey.str(text).scale(0.5f).color(textColor);
        }

        @Override
        public IDrawable getHoverBackground() {
            if (this.parent == null || this.parent.creatureNBT == null || this.parent.creatureNBT.nbtIsEmpty()) return IKey.NONE;
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
