package anightdazingzoroark.prift.client.ui.widgets;

import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.client.ui.RiftPartyScreen;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorage;
import anightdazingzoroark.prift.server.entity.creature.IRiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftPartyActionMessage;
import anightdazingzoroark.prift.server.properties.PlayerPartyProperties;
import anightdazingzoroark.prift.util.RiftUtil;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.menu.ContextMenuButton;
import com.cleanroommc.modularui.widgets.menu.Menu;
import com.cleanroommc.modularui.widgets.layout.Flow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PartyMemberButton extends ContextMenuButton<PartyMemberButton> {
    private static final int CARD_WIDTH = 98;
    private static final int CARD_HEIGHT = 48;
    private static final int ICON_SIZE = 34;
    private static final int BAR_WIDTH = 50;
    private static final int BAR_HEIGHT = 2;

    @NotNull
    private final RiftPartyScreen partyScreen;
    private final int partyIndex;

    public PartyMemberButton(@NotNull RiftPartyScreen partyScreen, int partyIndex) {
        super("party_member_" + partyIndex);
        this.partyScreen = partyScreen;
        this.partyIndex = partyIndex;
        this.requiresClick()
                .direction(getMenuDirection(partyIndex))
                .size(CARD_WIDTH, CARD_HEIGHT)
                .menu(new Menu<>().width(64).coverChildrenHeight()
                        .child(Flow.column().widthRel(1f).coverChildrenHeight()
                                .child(new ConditionalButtonWidget(this::canUseDeploymentButton).widthRel(1f).height(18)
                                        .child(IKey.dynamic(() -> {
                                                    CreatureNBT storedCreature = this.getStoredCreature();
                                                    //dismiss
                                                    if (storedCreature != null && storedCreature.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY) {
                                                        return IKey.lang("gui.prift.party.dropdown.dismiss").get();
                                                    }
                                                    //summon
                                                    String summonText = IKey.lang("gui.prift.party.dropdown.summon").get();
                                                    if (!this.canUseDeploymentButton()) {
                                                        return TextFormatting.STRIKETHROUGH + summonText + TextFormatting.RESET;
                                                    }
                                                    return summonText;
                                                }).scale(0.65f)
                                                .asWidget().widthRel(1f).heightRel(1f).padding(4, 1)
                                                .textAlign(Alignment.CENTER))
                                        .onMousePressed(button -> {
                                            this.closeMenu(false);
                                            RiftMessages.WRAPPER.sendToServer(new RiftPartyActionMessage(
                                                    RiftPartyActionMessage.Action.TOGGLE_PARTY_MEMBER, this.partyIndex, -1
                                            ));
                                            Interactable.playButtonClickSound();
                                            return true;
                                        })
                                        .tooltipDynamic(tooltip -> {
                                            IKey disabledMessage = this.getDeploymentDisabledMessage();
                                            if (disabledMessage != null) tooltip.addLine(disabledMessage);
                                        }))
                                .child(new Rectangle().color(0xFF555555).asWidget().widthRel(1f).height(1).margin(2, 0))
                                .child(new ButtonWidget<>().widthRel(1f).height(18)
                                        .child(IKey.lang("gui.prift.party.dropdown.inventory").scale(0.65f)
                                                .asWidget().widthRel(1f).heightRel(1f).padding(4, 1)
                                                .textAlign(Alignment.CENTER))
                                        .onMousePressed(button -> this.openCreaturePage(0)))
                                .child(new ButtonWidget<>().widthRel(1f).height(18)
                                        .child(IKey.lang("gui.prift.party.dropdown.settings").scale(0.65f)
                                                .asWidget().widthRel(1f).heightRel(1f).padding(4, 1)
                                                .textAlign(Alignment.CENTER))
                                        .onMousePressed(button -> this.openCreaturePage(1)))
                                .child(new ButtonWidget<>().widthRel(1f).height(18)
                                        .child(IKey.lang("gui.prift.party.dropdown.summary").scale(0.65f)
                                                .asWidget().widthRel(1f).heightRel(1f).padding(4, 1)
                                                .textAlign(Alignment.CENTER))
                                        .onMousePressed(button -> this.openCreaturePage(2)))
                                .child(new ButtonWidget<>().widthRel(1f).height(18)
                                        .child(IKey.lang("gui.prift.party.dropdown.moves").scale(0.65f)
                                                .asWidget().widthRel(1f).heightRel(1f).padding(4, 1)
                                                .textAlign(Alignment.CENTER))
                                        .onMousePressed(button -> this.openCreaturePage(3)))
                        ));
    }

    @NotNull
    private static Direction getMenuDirection(int partyIndex) {
        boolean leftColumn = partyIndex % 2 == 0;
        if (partyIndex >= 4) return leftColumn ? Direction.RIGHT_UP : Direction.LEFT_UP;
        return leftColumn ? Direction.RIGHT_DOWN : Direction.LEFT_DOWN;
    }

    private boolean openCreaturePage(int page) {
        this.closeMenu(false);
        this.partyScreen.openCreaturePage(this.partyIndex, page);
        return true;
    }

    private boolean canUseDeploymentButton() {
        CreatureNBT storedCreature = this.getStoredCreature();
        if (storedCreature == null || storedCreature.nbtTagCompound().isEmpty()) return false;
        if (storedCreature.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY) return true;
        if (storedCreature.getDeploymentType() != RiftCreatureEnums.CreatureDeployment.PARTY_INACTIVE) return false;
        if (storedCreature.getHealth() <= 0f) return false;

        PlayerPartyProperties playerParty = PlayerPartyProperties.get(Minecraft.getMinecraft().player);
        return playerParty != null && playerParty.canSummonAtCurrentPosition();
    }

    @Nullable
    private IKey getDeploymentDisabledMessage() {
        if (this.canUseDeploymentButton()) return null;
        CreatureNBT storedCreature = this.getStoredCreature();
        if (storedCreature == null || storedCreature.nbtTagCompound().isEmpty()) return null;
        if (storedCreature.getHealth() <= 0f) return IKey.lang("party.warning.cannot_summon_dead");
        return IKey.lang("party.warning.cannot_summon");
    }

    @Override
    @NotNull
    public Result onMousePressed(int mouseButton) {
        CreatureNBT storedCreature = this.getStoredCreature();
        if (this.partyScreen.isSorting()) {
            if (!this.partyScreen.selectSortPosition(this.partyIndex)) return Result.ACCEPT;
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        if (storedCreature == null || storedCreature.nbtTagCompound().isEmpty()) return Result.ACCEPT;

        Interactable.playButtonClickSound();
        return super.onMousePressed(mouseButton);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());
        CreatureNBT storedCreature = this.getStoredCreature();
        IRiftCreature creature = this.getDisplayedCreature(storedCreature);
        boolean empty = storedCreature == null || storedCreature.nbtTagCompound().isEmpty();
        boolean selected = this.partyScreen.getSelectedSortPosition() == this.partyIndex;

        new Rectangle().color(selected ? 0xFFFFD54F : this.isHovering() ? 0xFFFFFFFF : 0xFF000000).cornerRadius(6)
                .draw(context, 0, 0, CARD_WIDTH, CARD_HEIGHT, theme);
        if (empty || creature == null) {
            new Rectangle().color(0xFF555555).cornerRadius(5)
                    .draw(context, 1, 1, CARD_WIDTH - 2, CARD_HEIGHT - 2, theme);
            return;
        }

        new Rectangle().color(0xFFC6C6C6).cornerRadius(5)
                .draw(context, 1, 1, CARD_WIDTH - 2, CARD_HEIGHT - 2, theme);
        new Rectangle().color(selected ? 0xFFFFD54F : this.isHovering() ? 0xFFFFFFFF : 0xFF000000).cornerRadius(5)
                .draw(context, 5, 7, ICON_SIZE, ICON_SIZE, theme);
        new Rectangle().color(this.getIconBackgroundColor(storedCreature, creature)).cornerRadius(4)
                .draw(context, 6, 8, ICON_SIZE - 2, ICON_SIZE - 2, theme);
        CreatureIconTextures.getTextureForCreature(creature).draw(context, 10, 12, 24, 24, theme);

        IKey.str(creature.getName(false)).scale(0.5f).alignment(Alignment.CenterLeft).draw(context, 43, 5, 69, 10, theme);
        IKey.lang("gui.prift.creature_summary.level", creature.getLevel()).scale(0.4f).alignment(Alignment.CenterLeft)
                .draw(context, 43, 15, 69, 9, theme);
        this.drawBar(context, theme, 43, 27, creature.getHealth(), creature.getMaxHealth(), 0xFFFF0000);
        this.drawBar(context, theme, 43, 33, creature.getStamina(), creature.getMaxStamina(), 0xFFFFFF00);
        this.drawBar(context, theme, 43, 39, creature.getXP(), creature.getMaxXP(), 0xFF90EE90);
    }

    private void drawBar(
            @NotNull ModularGuiContext context, @NotNull WidgetTheme theme,
            int x, int y, float value, float maximum, int color
    ) {
        int filledWidth = maximum <= 0f ? 0 : Math.clamp(Math.round(value / maximum * BAR_WIDTH), 0, BAR_WIDTH);
        new Rectangle().color(0xFF868686).draw(context, x, y, BAR_WIDTH, BAR_HEIGHT, theme);
        if (filledWidth > 0) new Rectangle().color(color).draw(context, x, y, filledWidth, BAR_HEIGHT, theme);
    }

    private int getIconBackgroundColor(
            @Nullable CreatureNBT storedCreature, @Nullable IRiftCreature displayedCreature
    ) {
        if (storedCreature == null || storedCreature.nbtTagCompound().isEmpty()) return 0xFF555555;
        if (displayedCreature != null && displayedCreature.getHealth() <= 0f) return 0xFFF33F3F;
        if (storedCreature.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY) return 0xFF208620;
        return 0xFFC6C6C6;
    }

    @Nullable
    private CreatureNBT getStoredCreature() {
        PlayerPartyProperties playerParty = PlayerPartyProperties.get(Minecraft.getMinecraft().player);
        if (playerParty == null) return null;
        CreatureStorage creatureStorage = playerParty.getCreatureStorage();
        if (this.partyIndex < 0 || this.partyIndex >= creatureStorage.getSize()) return null;
        return creatureStorage.getCreature(this.partyIndex);
    }

    @Nullable
    private IRiftCreature getDisplayedCreature(@Nullable CreatureNBT storedCreature) {
        if (storedCreature == null || storedCreature.nbtTagCompound().isEmpty()) return null;
        Entity entity = RiftUtil.getEntityWithUUID(Minecraft.getMinecraft().world, storedCreature.getUniqueID());
        return entity instanceof RiftCreature creature ? creature : storedCreature;
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFallback();
    }
}
