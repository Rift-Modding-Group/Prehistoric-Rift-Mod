package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.widgets.PartyMemberButton;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorage;
import anightdazingzoroark.prift.server.entity.creature.IRiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiData;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiFactory;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftPartyActionMessage;
import anightdazingzoroark.prift.server.properties.PlayerPartyProperties;
import anightdazingzoroark.prift.util.RiftUtil;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class RiftPartyScreen extends CustomModularScreen {
    private boolean sorting;
    private int selectedSortPosition = -1;

    public RiftPartyScreen() {
        super(RiftInitialize.MODID);
    }

    @Override
    @NotNull
    public ModularPanel buildUI(ModularGuiContext context) {
        return new ModularPanel("rift_party").size(220, 200)
                .child(Flow.column().widthRel(1f).heightRel(1f).padding(9, 8).childPadding(6)
                        .child(Flow.row().widthRel(1f).height(20)
                                .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                .child(IKey.lang("gui.prift.party.title").scale(1.1f).asWidget())
                                .child(new CycleButtonWidget()
                                        .value(new BoolValue.Dynamic(() -> this.sorting, value -> {
                                            this.sorting = value;
                                            this.selectedSortPosition = -1;
                                        }))
                                        .stateBackground(false, GuiTextures.MC_BUTTON)
                                        .stateBackground(true, GuiTextures.MC_BUTTON_PRESSED)
                                        .stateHoverBackground(false, GuiTextures.MC_BUTTON_HOVERED)
                                        .stateHoverBackground(true, GuiTextures.MC_BUTTON_HOVERED_PRESSED)
                                        .size(18, 18)
                                        .overlay(GuiTextures.REFRESH.asIcon().size(12))
                                        .addTooltipLine(IKey.lang("gui.prift.party.sort"))
                                )
                        )
                        .child(new Grid().coverChildren().mapTo(2, PlayerPartyProperties.MAX_SIZE,
                                index -> new PartyMemberButton(this, index).margin(2, 2)
                        ))
                );
    }

    public boolean isSorting() {
        return this.sorting;
    }

    public int getSelectedSortPosition() {
        return this.selectedSortPosition;
    }

    public boolean selectSortPosition(int partyIndex) {
        PlayerPartyProperties playerParty = PlayerPartyProperties.get(Minecraft.getMinecraft().player);
        if (playerParty == null) return false;
        CreatureStorage creatureStorage = playerParty.getCreatureStorage();
        if (partyIndex < 0 || partyIndex >= creatureStorage.getSize()) return false;
        boolean empty = creatureStorage.getCreature(partyIndex).nbtTagCompound().isEmpty();

        if (this.selectedSortPosition < 0) {
            if (empty) return false;
            this.selectedSortPosition = partyIndex;
        }
        else if (this.selectedSortPosition == partyIndex) {
            this.selectedSortPosition = -1;
        }
        else {
            RiftMessages.WRAPPER.sendToServer(new RiftPartyActionMessage(
                    RiftPartyActionMessage.Action.SWAP, this.selectedSortPosition, partyIndex
            ));
            this.selectedSortPosition = -1;
        }
        return true;
    }

    public void openCreaturePage(int partyIndex, int page) {
        Minecraft minecraft = Minecraft.getMinecraft();
        PlayerPartyProperties playerParty = PlayerPartyProperties.get(minecraft.player);
        if (playerParty == null) return;
        CreatureStorage creatureStorage = playerParty.getCreatureStorage();
        if (partyIndex < 0 || partyIndex >= creatureStorage.getSize()) return;
        CreatureNBT storedCreature = creatureStorage.getCreature(partyIndex);
        if (storedCreature.nbtTagCompound().isEmpty()) return;

        Entity entity = RiftUtil.getEntityWithUUID(minecraft.world, storedCreature.getUniqueID());
        IRiftCreature creature = entity instanceof RiftCreature deployedCreature ? deployedCreature : storedCreature;
        GuiManager.openFromClient(
                RiftCreatureGuiFactory.INSTANCE,
                new RiftCreatureGuiData(minecraft.player, creature, partyIndex, page)
        );
    }
}
