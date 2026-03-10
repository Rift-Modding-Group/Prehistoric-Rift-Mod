package anightdazingzoroark.prift.client.newui.screens.player;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.newui.UIPanelNames;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.newui.panel.ModularPanelExitAffectable;
import anightdazingzoroark.prift.client.newui.value.HashMapValue;
import anightdazingzoroark.prift.client.newui.widget.PartyMemberButtonForPartyWidget;
import anightdazingzoroark.prift.client.newui.widget.PaddedGrid;
import anightdazingzoroark.prift.client.newui.widget.SideButton;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.ObjectValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Function;

public class RiftPartyScreen extends CustomModularScreen {
    private SelectedCreatureInfo.SwapInfo creatureSwapInfo = new SelectedCreatureInfo.SwapInfo();
    private boolean isCreatureSwitching;

    public RiftPartyScreen() {
        super(RiftInitialize.MODID);
    }

    @Override
    public @NotNull ModularPanel buildUI(ModularGuiContext context) {
        EntityPlayer player = Minecraft.getMinecraft().player;

        BoolValue.Dynamic creatureSwitchingDynamic = new BoolValue.Dynamic(
                () -> this.isCreatureSwitching,
                value -> this.isCreatureSwitching = value
        );
        ObjectValue.Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic = new ObjectValue.Dynamic<>(
                SelectedCreatureInfo.SwapInfo.class,
                () -> this.creatureSwapInfo,
                value -> this.creatureSwapInfo = value
        );

        return new ModularPanelExitAffectable(UIPanelNames.PARTY_SCREEN)
                .onEscPressed(new Function<ModularPanelExitAffectable, Boolean>() {
                    @Override
                    public Boolean apply(ModularPanelExitAffectable panel) {
                        if (!this.hasOpenedDropdown(panel)) return false;
                        this.closeAllPartyPanels(panel);
                        return true;
                    }

                    private boolean hasOpenedDropdown(ModularPanelExitAffectable panel) {
                        if (panel == null || !panel.isValid()) return false;
                        PaddedGrid grid = this.getGrid(panel);
                        if (grid == null || !grid.isValid()) return false;

                        for (IWidget child : grid.getChildren()) {
                            if (child instanceof PartyMemberButtonForPartyWidget partyMemberButton) {
                                if (partyMemberButton.getButtonMenu() == null) continue;
                                if (!partyMemberButton.getButtonMenu().getPanel().isValid()) continue;

                                if (partyMemberButton.getButtonMenu().getPanel().isOpen()) return true;
                            }
                        }
                        return false;
                    }

                    private void closeAllPartyPanels(ModularPanelExitAffectable panel) {
                        if (panel == null || !panel.isValid()) return;
                        PaddedGrid grid = this.getGrid(panel);
                        if (grid == null || !grid.isValid()) return;

                        for (IWidget child : grid.getChildren()) {
                            if (child instanceof PartyMemberButtonForPartyWidget partyMemberButton) {
                                if (partyMemberButton.getButtonMenu() == null) continue;
                                if (!partyMemberButton.getButtonMenu().getPanel().isValid()) continue;

                                partyMemberButton.closeButtonMenu();
                            }
                        }
                    }

                    private PaddedGrid getGrid(IWidget panel) {
                        if (panel == null || !panel.isValid()) return null;
                        for (IWidget child : panel.getChildren()) {
                            //search in self
                            if (child instanceof PaddedGrid grid) return grid;

                            //search in children too
                            IWidget recursionRes = this.getGrid(child);
                            if (recursionRes instanceof PaddedGrid recursionResGrid) return recursionResGrid;
                        }
                        return null;
                    }
                })
                .width(180).coverChildrenHeight()
                .child(Flow.column().coverChildren()
                        .leftRel(0f, 4, 1f)
                        .child(new SideButton()
                                .overlay(new ItemDrawable(Items.BOOK).asIcon())
                                .onMousePressed(button -> {
                                    PlayerUIHelper.openUI(player, UIPanelNames.JOURNAL_SCREEN);
                                    return true;
                                })
                                .addTooltipElement(IKey.lang("party.open_journal"))
                                .tab(GuiTextures.TAB_LEFT, -1)
                        )
                )
                .child(new ParentWidget<>().padding(7, 7)
                        .coverChildren()
                        .child(Flow.column().coverChildren()
                                .childPadding(5)
                                .child(new ParentWidget<>().coverChildrenHeight().widthRel(1f)
                                        .child(IKey.lang("party.label").asWidget().align(Alignment.CenterLeft))
                                        .child(new ToggleButton().overlay()
                                                .value(creatureSwitchingDynamic)
                                                .addTooltipElement(IKey.lang("party.swap_creatures"))
                                                .overlay(GuiTextures.REVERSE.asIcon().size(12))
                                                .size(12).right(0)
                                        )
                                )
                                .child(new PaddedGrid().coverChildren()
                                        .matrix(Grid.mapToMatrix(
                                                2, PlayerPartyHelper.maxSize,
                                                index -> new PartyMemberButtonForPartyWidget(
                                                        index,
                                                        player,
                                                        creatureSwapInfoDynamic,
                                                        creatureSwitchingDynamic
                                                )
                                        ))
                                        .padding(4)
                                )
                        )
                );
    }
}
