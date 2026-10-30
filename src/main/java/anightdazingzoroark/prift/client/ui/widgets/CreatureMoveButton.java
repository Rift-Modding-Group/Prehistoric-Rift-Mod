package anightdazingzoroark.prift.client.ui.widgets;

import anightdazingzoroark.prift.api.creature.CreatureMoveRegistry;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.client.ClientEnums;
import anightdazingzoroark.prift.client.ui.syncValues.CreatureMovesSyncHandler;
import anightdazingzoroark.prift.server.entity.creature.IRiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.math.BigDecimal;

/**
 * A move card whose icon tooltips take precedence over the move description.
 */
public class CreatureMoveButton extends ButtonWidget<CreatureMoveButton> implements IViewport {
    private final CreatureMovesSyncHandler moves;
    @NotNull
    private IRiftCreature creature;
    private final boolean bank;
    private final int index;
    private String displayedMove = "";
    private boolean displayedSwapping;

    public CreatureMoveButton(@NotNull CreatureMovesSyncHandler moves, @NotNull IRiftCreature creature, boolean bank, int index) {
        this.moves = moves;
        this.creature = creature;
        this.bank = bank;
        this.index = index;
        this.widthRel(1f).height(47).marginBottom(3)
                .disableThemeBackground(true).disableHoverBackground()
                .onMousePressed(button -> {
                    if (button != 0) return false;
                    CreatureMovesSyncHandler.selectSlot(this.moves, this.bank, this.index);
                    return true;
                })
                .tooltipDynamic(tooltip -> {
                    if (!this.displayedMove.isEmpty()) {
                        tooltip.addLine(IKey.lang("move.creature." + this.displayedMove + ".description"));
                    }
                });
        this.refreshContents();
    }

    @Override
    @NotNull
    public Result onMousePressed(int mouseButton) {
        if (!this.moves.isSwapping() || !this.moves.canSwap()) return Result.STOP;
        return super.onMousePressed(mouseButton);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        List<String> names = this.bank ? this.moves.getSnapshot().bank() : this.moves.getSnapshot().equipped();
        String name = this.index < names.size() ? names.get(this.index) : "";
        boolean enabled;
        if (this.bank) {
            enabled = this.index < names.size() || this.index == names.size() && (this.moves.isSwapping() || names.isEmpty());
        }
        else enabled = this.index < this.moves.getSnapshot().maximumSlots() && this.index <= names.size();
        this.setEnabled(enabled);
        if (!this.displayedMove.equals(name) || name.isEmpty() && this.displayedSwapping != this.moves.isSwapping())
            this.refreshContents();
    }

    private void refreshContents() {
        List<String> names = this.bank ? this.moves.getSnapshot().bank() : this.moves.getSnapshot().equipped();
        this.displayedMove = this.index < names.size() ? names.get(this.index) : "";
        this.displayedSwapping = this.moves.isSwapping();
        this.markTooltipDirty();
        CreatureMoveRegistry.MoveDefinition definition = CreatureMoveRegistry.getMove(this.displayedMove);
        if (definition == null) {
            if (this.moves.isSwapping()) {
                this.child(GuiTextures.ADD.asIcon().size(24).asWidget().center());
            }
            else {
                String label = this.bank ? "gui.prift.creature_moves.empty_bank" : "gui.prift.creature_moves.empty_slot";
                this.child(IKey.lang(label).scale(0.7f).asWidget().sizeRel(1f).textAlign(Alignment.CENTER));
            }
            return;
        }
        CreatureMoveBuilder move = definition.moveBuilder();
        ClientEnums.StaminaUse staminaUse = ClientEnums.StaminaUse.getUse(Math.max(move.getStaminaCost(), move.getStaminaDrainPerSecond()));
        this.child(new Flow(GuiAxis.Y) {
            @Override
            public boolean canHover() {
                return false;
            }
        }.sizeRel(1f).padding(4, 3).childPadding(1)
                .child(IKey.dynamic(() -> I18n.format("move.creature." + this.displayedMove)).scale(0.75f).asWidget()
                        .widthRel(1f).height(11).textAlign(Alignment.CENTER))
                .child(new Flow(GuiAxis.X) {
                    @Override
                    public boolean canHover() {
                        return false;
                    }
                }.widthRel(1f).height(16).childPadding(2)
                        .child(MoveIconTextures.getTextureForMoveType(move.getMoveType()))
                        .child(MoveIconTextures.getTextureForElementType(move.getElement()))
                        .child(IKey.dynamic(() -> {
                                    String power;
                                    if (move.getMoveType() == CreatureMoveBuilder.MoveType.STATUS) {
                                        power = I18n.format("gui.prift.not_applicable");
                                    }
                                    else {
                                        double basePower = CreatureMoveHelper.getBasePowerWithElementBonus(move, this.creature.getCreatureType().getElement());
                                        power = BigDecimal.valueOf(basePower).stripTrailingZeros().toPlainString();
                                        if (CreatureMoveHelper.hasSameElementAttackBonus(move, this.creature.getCreatureType().getElement())) {
                                            power = TextFormatting.BLUE + power + TextFormatting.RESET;
                                        }
                                    }
                                    return I18n.format("gui.prift.creature_moves.base_power", power);
                                })
                                .scale(0.65f).asWidget().left(36).right(0).height(16).textAlign(Alignment.CenterRight)))
                .child(new Flow(GuiAxis.X) {
                    @Override
                    public boolean canHover() {
                        return false;
                    }
                }.widthRel(1f).height(9).childPadding(1)
                        .child(IKey.lang("gui.prift.creature_moves.stamina",
                                        staminaUse == ClientEnums.StaminaUse.NONE ? I18n.format("gui.prift.not_applicable") : "")
                                .scale(0.65f).asWidget())
                        .children(staminaUse.ordinal(), ignored -> MoveIconTextures.getStaminaIcon())));
    }

    @Override
    public void postDraw(@NotNull ModularGuiContext context, boolean transformed) {
        // Tint after the icons and text, while still inside the list's clipping area.
        if (transformed || !this.moves.getSnapshot().busy()) return;
        new Rectangle().color(0x80999999).cornerRadius(6)
                .draw(context, 0, 0, this.getArea().width, this.getArea().height,
                        this.getActiveWidgetTheme(this.getWidgetTheme(this.getPanel().getTheme()), false));
    }

    @Override
    public void draw(@NotNull ModularGuiContext context, @NotNull WidgetThemeEntry<?> widgetTheme) {
        WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());
        int borderColor = 0xFF000000;
        if (this.moves.getSnapshot().busy()) borderColor = 0xFF555555;
        else if (this.moves.isSelected(this.bank, this.index)) borderColor = 0xFFFFD54F;
        else if (this.moves.isSwapping() && this.isHovering()) borderColor = 0xFFFFFFFF;
        new Rectangle().color(borderColor).cornerRadius(6)
                .draw(context, 0, 0, this.getArea().width, this.getArea().height, theme);
        new Rectangle().color(this.moves.getSnapshot().busy() ? 0xFF999999 : 0xFFC6C6C6).cornerRadius(5)
                .draw(context, 1, 1, this.getArea().width - 2, this.getArea().height - 2, theme);
    }

    @Override
    @NotNull
    public WidgetThemeEntry<?> getWidgetThemeInternal(@NotNull ITheme theme) {
        return theme.getFallback();
    }
}
