package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.newui.RiftCreatureScreen;
import anightdazingzoroark.prift.client.newui.UIColors;
import anightdazingzoroark.prift.client.newui.UIPanelNames;
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
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.menu.ContextMenuButton;
import com.cleanroommc.modularui.widgets.menu.Menu;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.*;

public class PartyMemberButtonWidget extends ContextMenuButton<PartyMemberButtonWidget> implements Interactable {
    private final int index;
    private CreatureNBT creatureNBT;

    public PartyMemberButtonWidget(int indexIn) {
        super(UIPanelNames.PARTY_DROPDOWN);
        this.index = indexIn;
        this.requiresClick();
        this.size(80, 48);
        this.openDown();
        this.menuList(list -> {
            list.name("options").width(64).coverChildrenHeight().children(
                    Arrays.asList(Option.values()), option -> new PartyMemberDropdownOptionWidget(index, option)
            );
        });
    }

    @Override
    public void onInit() {
        super.onInit();
        this.setCreatureNBT();
    }

    private void setCreatureNBT() {
        this.creatureNBT = PlayerTamedCreaturesHelper.getCreatureNBTFromSelected(
                Minecraft.getMinecraft().player,
                new SelectedCreatureInfo(SelectedCreatureInfo.SelectedPosType.PARTY, new int[]{this.index})
        );
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());
        float textScale = 0.5f;

        if (this.creatureNBT != null && !this.creatureNBT.nbtIsEmpty()) {
            //draw border, color changes depending on whether or not its hovered
            int borderHoverColor = this.isHovering() ? 0xFFFFFFFF : 0xFF000000;
            new Rectangle().color(borderHoverColor).cornerRadius(5).drawAtZero(context, this.getArea(), theme);

            //draw background
            Area bgArea = new Area(this.getArea());
            bgArea.h(bgArea.h() - 2);
            bgArea.w(bgArea.w() - 2);
            new Rectangle().color(0xFFC6C6C6).cornerRadius(5).draw(context, 1, 1, bgArea.w(), bgArea.h(), theme);

            //draw container for creature icon
            int iconHoverColor = this.isHovering() ? 0xFFFFFFFF : 0xFF000000;
            new Rectangle().color(iconHoverColor).cornerRadius(5).draw(context, 2, 2, 28, 28, theme);
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

    private int getIconBGColor() {
        if (this.creatureNBT.nbtIsEmpty()) return -1;
        else if (this.creatureNBT.getCreatureHealth()[0] <= 0) return 0xFFF33F3F;
        else if (this.creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) return 0xFF208620;
        return 0xFFC6C6C6;
    }

    private enum Option {
        INVENTORY((index, creatureNBTIn) -> {
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
        }),
        OPTIONS((index, creatureNBTIn) -> {
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
        }),
        INFO((index, creatureNBTIn) -> {
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
        }),
        MOVES((index, creatureNBTIn) -> {
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
        }),
        SUMMON_OR_DISMISS((index, creatureNBTIn) -> false),
        TELEPORT((index, creatureNBTIn) -> false);

        private final BiFunction<Integer, CreatureNBT, Boolean> clickResult;

        Option(BiFunction<Integer, CreatureNBT, Boolean> clickResult) {
            this.clickResult = clickResult;
        }

        public boolean click(int index, CreatureNBT creatureNBT) {
            return this.clickResult.apply(index, creatureNBT);
        }
    }

    private static class PartyMemberDropdownOptionWidget extends ButtonWidget<PartyMemberDropdownOptionWidget> {
        private final int parentIndex;
        private final Option option;
        private CreatureNBT creatureNBT;

        public PartyMemberDropdownOptionWidget(int parentIndex, Option optionIn) {
            this.parentIndex = parentIndex;
            this.option = optionIn;
            this.widthRel(1f).height(10);
            this.overlay(IKey.str(option.name()).scale(0.5f));
        }

        @Override
        public void onInit() {
            super.onInit();
            this.creatureNBT = PlayerTamedCreaturesHelper.getCreatureNBTFromSelected(
                    Minecraft.getMinecraft().player,
                    new SelectedCreatureInfo(SelectedCreatureInfo.SelectedPosType.PARTY, new int[]{this.parentIndex})
            );
        }

        @Override
        @NotNull
        public Result onMousePressed(int mouseButton) {
            if (this.creatureNBT != null && !this.creatureNBT.nbtIsEmpty() && this.option.click(this.parentIndex, this.creatureNBT)) {
                Interactable.playButtonClickSound();
                return Result.SUCCESS;
            }
            return Result.ACCEPT;
        }

        @Override
        public IDrawable getOverlay() {
            int textColor = this.isHovering() ? 0xFFFFFFFF : IKey.TEXT_COLOR;
            return IKey.str(this.option.name()).scale(0.5f).color(textColor);
        }

        @Override
        public IDrawable getHoverBackground() {
            return new DrawableStack(
                    new Rectangle().color(0xFFB5377B),
                    new Rectangle().color(0xFF942C64).hollow(0.5f)
            );
        }
    }
}
