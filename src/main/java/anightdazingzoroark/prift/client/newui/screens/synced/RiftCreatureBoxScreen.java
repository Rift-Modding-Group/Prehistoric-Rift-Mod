package anightdazingzoroark.prift.client.newui.screens.synced;

import anightdazingzoroark.prift.client.newui.UIPanelNames;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.newui.value.FixedSizeCreatureListSyncValue;
import anightdazingzoroark.prift.client.newui.value.HashMapValue;
import anightdazingzoroark.prift.client.newui.widget.CreatureInBoxButtonWidget;
import anightdazingzoroark.prift.client.newui.widget.PaddedGrid;
import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.CreatureBoxStorage;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxHelper;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.ObjectValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

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

        BoolValue.Dynamic creatureSwitchingDynamic = new BoolValue.Dynamic(
                teCreatureBox::getIsCreatureSwitching,
                teCreatureBox::setIsCreatureSwitching
        );
        ObjectValue.Dynamic<SelectedCreatureInfo.SwapInfo> creatureSwapInfoDynamic = new ObjectValue.Dynamic<>(
                SelectedCreatureInfo.SwapInfo.class,
                teCreatureBox::getCreatureSwapInfo,
                teCreatureBox::setCreatureSwapInfo
        );
        HashMapValue.Dynamic<Integer, RiftCreature> deployedPartyCreaturesDynamic = new HashMapValue.Dynamic<>(
                teCreatureBox::getDeployedPartyCreatures,
                teCreatureBox::setDeployedPartyCreatures
        );

        //synced stuff
        FixedSizeCreatureListSyncValue creatureBoxDeployed = new FixedSizeCreatureListSyncValue(
                teCreatureBox::getDeployedCreatures,
                teCreatureBox::setDeployedCreatures
        );
        syncManager.syncValue("creatureBoxDeployed", creatureBoxDeployed);

        return new ModularPanel(UIPanelNames.CREATURE_BOX_SCREEN).size(220, 200)
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
                                                        creatureSwitchingDynamic,
                                                        creatureSwapInfoDynamic,
                                                        deployedPartyCreaturesDynamic
                                                )
                                        ))
                                        .padding(2)
                                )
                        )
                )
                //middle side will be box creatures
                .child(new ParentWidget<>().name("boxSection").coverChildren().center()
                        .child(Flow.column().margin(5).coverChildren().childPadding(5)
                                .child(new ParentWidget<>().name("BoxSectionHeader").size(168, 18)
                                        .child(Flow.row().coverChildren().childPadding(3).center()
                                                .child(new ButtonWidget<>())
                                                .child(IKey.dynamic(() -> {
                                                    return playerBox.getCreatureBoxStorage().getBoxName(currentBoxIndexDynamic.getIntValue());
                                                }).asWidget())
                                                .child(new ButtonWidget<>())
                                        )
                                )
                                .child(new PaddedGrid().name("BoxMembersGrid").coverChildren()
                                        .matrix(Grid.mapToMatrix(
                                                5, CreatureBoxStorage.maxBoxStorableCreatures,
                                                index -> new CreatureInBoxButtonWidget(
                                                        playerBox, currentBoxIndexDynamic, index,
                                                        creatureSwitchingDynamic,
                                                        creatureSwapInfoDynamic,
                                                        deployedPartyCreaturesDynamic
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
                                                        creatureSwitchingDynamic,
                                                        creatureSwapInfoDynamic,
                                                        deployedPartyCreaturesDynamic
                                                )
                                        ))
                                        .padding(2)
                                )
                        )
                );
    }
}
