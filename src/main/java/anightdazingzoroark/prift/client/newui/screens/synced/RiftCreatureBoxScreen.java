package anightdazingzoroark.prift.client.newui.screens.synced;

import anightdazingzoroark.prift.client.newui.RiftUIIcons;
import anightdazingzoroark.prift.client.newui.UIPanelNames;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.newui.panel.ModularPanelExitAffectable;
import anightdazingzoroark.prift.client.newui.value.FixedSizeCreatureListSyncValue;
import anightdazingzoroark.prift.client.newui.widget.CreatureInBoxButtonWidget;
import anightdazingzoroark.prift.client.newui.widget.PaddedGrid;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.CreatureBoxStorage;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxHelper;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.drawable.DrawableStack;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.ObjectValue;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.Dialog;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

public class RiftCreatureBoxScreen {
    public static ModularPanel buildCreatureBoxUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.getRecipeViewerSettings().disable();

        //get the creature box
        TileEntity tileEntity = data.getTileEntity();
        if (!(tileEntity instanceof RiftTileEntityCreatureBox teCreatureBox)) return new ModularPanel(UIPanelNames.CREATURE_BOX_SCREEN);

        //get player party and box info
        EntityPlayer player = data.getPlayer();
        PlayerPartyProperties playerParty = PlayerPartyHelper.getPlayerParty(player);
        PlayerCreatureBoxProperties playerBox = PlayerCreatureBoxHelper.getPlayerCreatureBox(player);

        //dynamic stuff
        IntValue.Dynamic currentBoxIndexDynamic = new IntValue.Dynamic(
                teCreatureBox::getCurrentBoxIndex,
                teCreatureBox::setCurrentBoxIndex
        );
        ObjectValue.Dynamic<SelectedCreatureInfo> selectedCreatureInfoDynamic = new ObjectValue.Dynamic<>(
                SelectedCreatureInfo.class,
                teCreatureBox::getSelectedCreatureInfo,
                teCreatureBox::setSelectedCreatureInfo
        );
        BoolValue.Dynamic creatureSwitchingDynamic = new BoolValue.Dynamic(
                teCreatureBox::getIsCreatureSwitching,
                teCreatureBox::setIsCreatureSwitching
        );
        ObjectValue.Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic = new ObjectValue.Dynamic<>(
                SelectedCreatureInfo.SwapInfo.class,
                teCreatureBox::getCreatureSwapInfo,
                teCreatureBox::setCreatureSwapInfo
        );

        //synced stuff
        FixedSizeCreatureListSyncValue creatureBoxDeployed = new FixedSizeCreatureListSyncValue(
                teCreatureBox::getDeployedCreatures,
                teCreatureBox::setDeployedCreatures
        );
        syncManager.syncValue("creatureBoxDeployed", creatureBoxDeployed);

        //panel stuff
        IPanelHandler changeNamePanel = syncManager.syncedPanel(
                "changeNamePanel", true,
                (panelSyncMan, panelHandler) -> {
                    return boxNamePopupPanel(playerBox, currentBoxIndexDynamic);
                }
        );
        IPanelHandler inventoryDropPopupPanel = syncManager.syncedPanel(
                "inventoryDropPopupPanel", true,
                (panelSyncMan, panelHandler) -> {
                    return inventoryDropPopupPanel(player, creatureSwapInfoDynamic, selectedCreatureInfoDynamic);
                }
        );

        return new ModularPanelExitAffectable(UIPanelNames.CREATURE_BOX_SCREEN)
                .onEscPressed(panel -> {
                    selectedCreatureInfoDynamic.setValue(null);
                    creatureSwitchingDynamic.setBoolValue(false);
                    return false;
                })
                .onUpdateListener(panel -> {
                    //check if any of the swap positions have full inventories and are from the party
                    if (creatureSwapInfoDynamic.getValue().canSwap()) {
                        //flag for opening inventoryDropPopupPanel
                        boolean alertToDeployedInventory = false;

                        if (creatureSwapInfoDynamic.getValue().getCreatureOne().selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY
                            && creatureSwapInfoDynamic.getValue().getCreatureTwo().selectedPosType != SelectedCreatureInfo.SelectedPosType.PARTY
                        ) {
                            int creatureOneIndex = creatureSwapInfoDynamic.getValue().getCreatureOne().getIndex();
                            CreatureNBT creatureNBT = playerParty.getPartyMember(creatureOneIndex);
                            if (playerParty.getDeployedPartyMemberMap().containsKey(creatureOneIndex)) {
                                creatureNBT = new CreatureNBT(playerParty.getLoadedDeployedCreature(creatureOneIndex));
                            }

                            alertToDeployedInventory = !creatureNBT.inventoryIsEmpty();
                        }
                        else if (creatureSwapInfoDynamic.getValue().getCreatureOne().selectedPosType != SelectedCreatureInfo.SelectedPosType.PARTY
                                && creatureSwapInfoDynamic.getValue().getCreatureTwo().selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY
                        ) {
                            int creatureTwoIndex = creatureSwapInfoDynamic.getValue().getCreatureTwo().getIndex();
                            CreatureNBT creatureNBT = playerParty.getPartyMember(creatureTwoIndex);
                            if (playerParty.getDeployedPartyMemberMap().containsKey(creatureTwoIndex)) {
                                creatureNBT = new CreatureNBT(playerParty.getLoadedDeployedCreature(creatureTwoIndex));
                            }

                            alertToDeployedInventory = !creatureNBT.inventoryIsEmpty();
                        }

                        //-----final decision-----
                        //show the inventory drop confirmation panel
                        if (alertToDeployedInventory) inventoryDropPopupPanel.openPanel();
                        //normal swapping
                        else {
                            PlayerPartyHelper.applyCreatureSwapClient(player, creatureSwapInfoDynamic.getValue());
                            creatureSwapInfoDynamic.getValue().clear();
                            selectedCreatureInfoDynamic.setValue(null);
                        }
                    }
                })
                .size(220, 200)
                //left side will be player party
                .child(new ParentWidget<>().name("partySection").coverChildren()
                        .background(GuiTextures.MC_BACKGROUND)
                        .leftRel(0f, 4, 1.1f)
                        .child(Flow.column().margin(5).coverChildren().childPadding(5)
                                .child(IKey.lang("box.party_label").asWidget())
                                .child(new PaddedGrid().coverChildren()
                                        .matrix(Grid.mapToMatrix(
                                                2, PlayerPartyHelper.maxSize,
                                                index -> new CreatureInBoxButtonWidget(
                                                        playerParty, index,
                                                        selectedCreatureInfoDynamic,
                                                        creatureSwitchingDynamic,
                                                        creatureSwapInfoDynamic
                                                )
                                        ))
                                        .padding(2)
                                )
                        )
                )
                //middle side will be box creatures and switch button
                .child(new ParentWidget<>().name("boxSection").coverChildren().center()
                        .child(new ToggleButton() {
                                    @Override
                                    public @NotNull Result onMousePressed(int mouseButton) {
                                        //reset swap info and selection info
                                        selectedCreatureInfoDynamic.setValue(null);
                                        creatureSwapInfoDynamic.getValue().clear();

                                        return super.onMousePressed(mouseButton);
                                    }
                                }.overlay(GuiTextures.REVERSE.asIcon())
                                .align(Alignment.TopRight).margin(5)
                                .addTooltipElement(IKey.lang("box.swap_creatures_tooltip"))
                                .value(creatureSwitchingDynamic)
                        )
                        .child(Flow.column().margin(5).coverChildren().childPadding(5)
                                .child(new ParentWidget<>().name("BoxSectionHeader").size(160, 18)
                                        .child(Flow.row().coverChildren().childPadding(3).center()
                                                .child(new ButtonWidget<>()
                                                        .overlay(RiftUIIcons.LEFT_ARROW.asIcon())
                                                        .hoverOverlay(RiftUIIcons.LEFT_ARROW_SELECTED.asIcon())
                                                        .background(IDrawable.EMPTY)
                                                        .hoverBackground(IDrawable.EMPTY)
                                                        .onMousePressed(
                                                        button -> {
                                                            int currentVal = currentBoxIndexDynamic.getIntValue();
                                                            int prevValue = currentVal - 1 >= 0 ? currentVal - 1 : CreatureBoxStorage.maxBoxAmnt - 1;
                                                            currentBoxIndexDynamic.setIntValue(prevValue);
                                                            return true;
                                                        })
                                                )
                                                .child(new ParentWidget<>().width(120).heightRel(1f)
                                                        .child(new ButtonWidget<>()
                                                                .width(120).heightRel(1f).center()
                                                                .overlay(new DrawableStack(
                                                                        new Rectangle().color(0xFF434343),
                                                                        new Rectangle().color(0xFF000000).hollow(),
                                                                        IKey.dynamic(() -> {
                                                                            return playerBox.getCreatureBoxStorage()
                                                                                    .getBoxName(currentBoxIndexDynamic.getIntValue());
                                                                        })
                                                                ))
                                                                .hoverOverlay(new DrawableStack(
                                                                        new Rectangle().color(0xFF434343),
                                                                        new Rectangle().color(0xFFFFFFFF).hollow(),
                                                                        IKey.dynamic(() -> {
                                                                            return playerBox.getCreatureBoxStorage()
                                                                                    .getBoxName(currentBoxIndexDynamic.getIntValue());
                                                                        })
                                                                ))
                                                                .background(IDrawable.EMPTY)
                                                                .hoverBackground(IDrawable.EMPTY)
                                                                .onMousePressed(mouseButton -> {
                                                                    changeNamePanel.openPanel();
                                                                    return true;
                                                                })
                                                                .addTooltipLine(IKey.lang("box.change_box_name_tooltip"))
                                                        )
                                                )
                                                .child(new ButtonWidget<>()
                                                        .overlay(RiftUIIcons.RIGHT_ARROW.asIcon())
                                                        .hoverOverlay(RiftUIIcons.RIGHT_ARROW_SELECTED.asIcon())
                                                        .background(IDrawable.EMPTY)
                                                        .hoverBackground(IDrawable.EMPTY)
                                                        .onMousePressed(
                                                        button -> {
                                                            int currentVal = currentBoxIndexDynamic.getIntValue();
                                                            int nextValue = currentVal + 1 < CreatureBoxStorage.maxBoxAmnt ? currentVal + 1 : 0;
                                                            currentBoxIndexDynamic.setIntValue(nextValue);
                                                            return true;
                                                        })
                                                )
                                        )
                                )
                                .child(new PaddedGrid().name("BoxMembersGrid").coverChildren()
                                        .matrix(Grid.mapToMatrix(
                                                5, CreatureBoxStorage.maxBoxStorableCreatures,
                                                index -> new CreatureInBoxButtonWidget(
                                                        playerBox, currentBoxIndexDynamic, index,
                                                        selectedCreatureInfoDynamic,
                                                        creatureSwitchingDynamic,
                                                        creatureSwapInfoDynamic
                                                ).size(40)
                                        ))
                                        .padding(2)
                                )
                        )
                )
                //right side will be box deployed creatures
                .child(new ParentWidget<>().name("deployedSection").coverChildren()
                        .background(GuiTextures.MC_BACKGROUND)
                        .rightRel(0f, 4, 1.1f)
                        .child(Flow.column().margin(5).coverChildren().childPadding(5)
                                .child(IKey.lang("box.deployed_label").asWidget())
                                .child(new PaddedGrid().coverChildren()
                                        .matrix(Grid.mapToMatrix(
                                                2, RiftCreatureBox.maxDeployableCreatures,
                                                index -> new CreatureInBoxButtonWidget(
                                                        creatureBoxDeployed, index,
                                                        selectedCreatureInfoDynamic,
                                                        creatureSwitchingDynamic,
                                                        creatureSwapInfoDynamic
                                                )
                                        ))
                                        .padding(2)
                                )
                        )
                );
    }

    private static ModularPanel boxNamePopupPanel(PlayerCreatureBoxProperties playerBox, IntValue.Dynamic currentBoxIndexDynamic) {
        Dialog<?> toReturn = new Dialog<>("boxNamePopup", null);
        TextFieldWidget nameTextBox = new TextFieldWidget().widthRel(0.75f)
                .value(new StringValue.Dynamic(
                        () -> playerBox.getCreatureBoxStorage().getBoxName(currentBoxIndexDynamic.getIntValue()),
                        value -> {}
                ));

        return toReturn.setDisablePanelsBelow(true)
                .setCloseOnOutOfBoundsClick(false)
                .size(160, 100)
                .padding(5)
                .child(ButtonWidget.panelCloseButton())
                .child(Flow.column().coverChildrenHeight().widthRel(1f).childPadding(15).center()
                        .child(IKey.lang("box.change_box_name").asWidget())
                        .child(nameTextBox)
                        .child(Flow.row().coverChildren().childPadding(5)
                                .child(new ButtonWidget<>().width(48)
                                        .overlay(IKey.lang("choice.confirm"))
                                        .onMousePressed(mouseButton -> {
                                            PlayerCreatureBoxHelper.changeBoxNameClient(
                                                    playerBox.getEntityHolder(),
                                                    currentBoxIndexDynamic.getIntValue(),
                                                    nameTextBox.getText()
                                            );
                                            toReturn.getPanel().closeIfOpen();
                                            return true;
                                        })
                                )
                                .child(new ButtonWidget<>().width(48)
                                        .overlay(IKey.lang("choice.cancel"))
                                        .onMousePressed(mouseButton -> {
                                            toReturn.getPanel().closeIfOpen();
                                            return true;
                                        })
                                )
                        )
                );
    }

    private static ModularPanel inventoryDropPopupPanel(
            EntityPlayer player,
            ObjectValue.Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic,
            ObjectValue.Dynamic<SelectedCreatureInfo> selectedCreatureInfoDynamic
    ) {
        Dialog<?> toReturn = new Dialog<>("inventoryDropPopupPopup", null) {
            @Override
            public void closeIfOpen() {
                creatureSwapInfoDynamic.getValue().clear();
                selectedCreatureInfoDynamic.setValue(null);
                super.closeIfOpen();
            }
        };
        IGuiAction.MousePressed panelCloseEffect = mouseButton -> {
            toReturn.getPanel().closeIfOpen();
            return true;
        };

        return toReturn.setDisablePanelsBelow(true)
                .setCloseOnOutOfBoundsClick(false)
                .size(160, 100)
                .padding(5)
                .child(ButtonWidget.panelCloseButton()
                        .onMousePressed(panelCloseEffect)
                )
                .child(Flow.column().coverChildrenHeight().widthRel(1f).childPadding(15).center()
                        .child(IKey.lang("box.warning.swap_remove_inventory").asWidget())
                        .child(Flow.row().coverChildren().childPadding(5)
                                .child(new ButtonWidget<>().width(48)
                                        .overlay(IKey.lang("choice.confirm"))
                                        .onMousePressed(mouseButton -> {
                                            PlayerPartyHelper.applyCreatureSwapClient(player, creatureSwapInfoDynamic.getValue());
                                            toReturn.getPanel().closeIfOpen();
                                            return true;
                                        })
                                )
                                .child(new ButtonWidget<>().width(48)
                                        .overlay(IKey.lang("choice.cancel"))
                                        .onMousePressed(panelCloseEffect)
                                )
                        )
                );
    }
}
