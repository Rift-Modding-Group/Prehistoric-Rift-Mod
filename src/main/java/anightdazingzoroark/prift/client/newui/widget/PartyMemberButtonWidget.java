package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.newui.RiftCreatureScreen;
import anightdazingzoroark.prift.client.newui.UIColors;
import anightdazingzoroark.prift.client.newui.UIPanelNames;
import anightdazingzoroark.prift.client.newui.data.PlayerGuiData;
import anightdazingzoroark.prift.client.newui.sync.CreatureSwapInfoSyncValue;
import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
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
    private final PlayerGuiData data;
    private final BooleanSyncValue isCreatureSwitching;
    private final CreatureSwapInfoSyncValue swapInfo;
    private final SelectedCreatureInfo selectedCreatureInfo;
    private boolean isSelected;
    private CreatureNBT creatureNBT;

    public PartyMemberButtonWidget(int indexIn, PlayerGuiData data, BooleanSyncValue isCreatureSwitching, CreatureSwapInfoSyncValue swapInfo) {
        super(UIPanelNames.PARTY_DROPDOWN+indexIn);
        this.index = indexIn;
        this.data = data;
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
    public void onInit() {
        super.onInit();
        this.setCreatureNBT();
    }

    @Override
    @NotNull
    public Result onMousePressed(int mouseButton) {
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
            System.out.println("select for swapping");
            this.swapInfo.getValue().setCreature(this.selectedCreatureInfo);
        }
        if (this.swapInfo.getValue().canSwap()) {
            System.out.println("start swapping");
            this.swapInfo.getValue().applySwap(this.data);
            this.setCreatureNBT();
            this.isSelected = false;
        }
        return Result.SUCCESS;
    }

    private void setCreatureNBT() {
        this.creatureNBT = PlayerTamedCreaturesHelper.getCreatureNBTFromSelected(
                Minecraft.getMinecraft().player, this.selectedCreatureInfo
        );
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
                creatureNBTIn -> true,
                (index, creatureNBTIn) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    RiftCreature creature = creatureNBTIn.findCorrespondingCreature(player.world);
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
                creatureNBTIn -> true,
                (index, creatureNBTIn) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    RiftCreature creature = creatureNBTIn.findCorrespondingCreature(player.world);
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
                creatureNBTIn -> true,
                (index, creatureNBTIn) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    RiftCreature creature = creatureNBTIn.findCorrespondingCreature(player.world);
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
                creatureNBTIn -> true,
                (index, creatureNBTIn) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    RiftCreature creature = creatureNBTIn.findCorrespondingCreature(player.world);
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
                creatureNBTIn -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    RiftCreature creature = creatureNBTIn.findCorrespondingCreature(player.world);
                    //if creature is found, almost anything may be done with it
                    if (creature != null) return true;
                    else {
                        //dont summon when creature is dead
                        if (creatureNBTIn.getCreatureHealth()[0] <= 0) return false;
                        //dont summon when player not in apt position
                        else if (!PlayerTamedCreaturesHelper.canBeDeployed(player, creatureNBTIn)) return false;
                        //final return value
                        else return true;
                    }
                },
                (index, creatureNBTIn) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    RiftCreature creature = creatureNBTIn.findCorrespondingCreature(player.world);

                    //creature is not found, time to try summon it
                    if (creature == null) {
                        //for summoning when creature is not found and conditions are just right
                        PlayerTamedCreaturesHelper.deployCreatureFromParty(player, index, true);
                        return true;
                    }
                    else {
                        //for dismissing, when creature is deployed
                        if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                            PlayerTamedCreaturesHelper.deployCreatureFromParty(player, index, false);
                            return true;
                        }
                    }
                    return false;
                },
                creatureNBTIn -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    if (creatureNBTIn.getCreatureHealth()[0] <= 0) return I18n.format("party.warning.cannot_summon_dead");
                    else if (!PlayerTamedCreaturesHelper.canBeDeployed(player, creatureNBTIn)) return I18n.format("party.warning.cannot_summon");
                    return "";
                }
        ),
        TELEPORT(
                creatureNBTIn -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    RiftCreature creature = creatureNBTIn.findCorrespondingCreature(player.world);

                    //return value is based on if creature exists in the world or not
                    return creature != null;
                },
                (index, creatureNBTIn) -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    RiftCreature creature = creatureNBTIn.findCorrespondingCreature(player.world);

                    if (creature != null) {
                        //check first if the creature can be teleported to that spot first
                        if (PlayerTamedCreaturesHelper.canBeDeployed(player, creatureNBTIn)) {
                            PlayerTamedCreaturesHelper.teleportCreatureToPlayer(player, index);
                            return true;
                        }
                    }
                    return false;
                },
                creatureNBTIn -> I18n.format("party.warning.cannot_teleport")
        );

        private final Function<CreatureNBT, Boolean> canBeClicked;
        private final BiFunction<Integer, CreatureNBT, Boolean> clickResult;
        private final Function<CreatureNBT, String> ineligibilityText;

        Option(Function<CreatureNBT, Boolean> canBeClicked, BiFunction<Integer, CreatureNBT, Boolean> clickResult) {
            this(canBeClicked, clickResult, null);
        }

        Option(Function<CreatureNBT, Boolean> canBeClicked, BiFunction<Integer, CreatureNBT, Boolean> clickResult, Function<CreatureNBT, String> ineligibilityText) {
            this.canBeClicked = canBeClicked;
            this.clickResult = clickResult;
            this.ineligibilityText = ineligibilityText;
        }

        public boolean canBeClicked(CreatureNBT creatureNBT) {
            return this.canBeClicked.apply(creatureNBT);
        }

        public boolean click(int index, CreatureNBT creatureNBT) {
            return this.clickResult.apply(index, creatureNBT);
        }

        public String getIneligibilityText(CreatureNBT creatureNBT) {
            if (this.ineligibilityText == null) return "";
            return this.ineligibilityText.apply(creatureNBT);
        }

        public boolean hasIneligibilityText() {
            return this.ineligibilityText != null;
        }

        public String getTranslatedName(CreatureNBT creatureNBT) {
            String strikethrough = !this.canBeClicked.apply(creatureNBT) ? IKey.STRIKETHROUGH.toString() : "";
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
                if (this.option.hasIneligibilityText() && !this.option.canBeClicked(this.parent.creatureNBT)) {
                    String result = this.option.getIneligibilityText(this.parent.creatureNBT);
                    if (!result.isEmpty()) tooltipBuilder.addLine(result);
                }
            });
        }

        @Override
        @NotNull
        public Result onMousePressed(int mouseButton) {
            if (this.parent == null) return Result.ACCEPT;
            if (this.parent.creatureNBT == null || this.parent.creatureNBT.nbtIsEmpty()) return Result.ACCEPT;
            if (!this.option.canBeClicked(this.parent.creatureNBT)) return Result.ACCEPT;
            if (this.option.click(this.parent.index, this.parent.creatureNBT)) {
                Interactable.playButtonClickSound();
                this.parent.setCreatureNBT();
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
            String text = this.option.getTranslatedName(this.parent.creatureNBT);
            int textColor = this.isHovering() ? 0xFFFFFFFF : IKey.TEXT_COLOR;
            return IKey.str(text).scale(0.5f).color(textColor);
        }

        @Override
        public IDrawable getHoverBackground() {
            if (this.parent == null || this.parent.creatureNBT == null || this.parent.creatureNBT.nbtIsEmpty()) return IKey.NONE;
            if (this.option.canBeClicked(this.parent.creatureNBT)) {
                return new DrawableStack(
                        new Rectangle().color(0xFFB5377B),
                        new Rectangle().color(0xFF942C64).hollow(0.5f)
                );
            }
            return IKey.NONE;
        }
    }
}
