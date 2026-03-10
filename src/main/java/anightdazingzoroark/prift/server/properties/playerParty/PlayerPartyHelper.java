package anightdazingzoroark.prift.server.properties.playerParty;

import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.propertySystem.Property;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.*;
import anightdazingzoroark.prift.server.properties.RiftPropertyRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;

public class PlayerPartyHelper {
    public static final int maxSize = 6;
    //this is for storing creatures that got deployed
    public static final HashMap<Integer, RiftCreature> deployedCreatures = new HashMap<>();

    public static PlayerPartyProperties getPlayerParty(EntityPlayer player) {
        return Property.getProperty(RiftPropertyRegistry.PLAYER_PARTY, player);
    }

    public static void applyCreatureSwapClient(EntityPlayer player, SelectedCreatureInfo.SwapInfo swapInfo) {
        if (!player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftApplyCreatureSwap(player, swapInfo));
    }

    public static void deployCreatureClient(EntityPlayer player, int index, boolean deploy) {
        if (!player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftDeployPartyMem(player, index, deploy));
    }

    public static void teleportCreatureClient(EntityPlayer player, int index) {
        if (!player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftTeleportPartyMemToPlayer(player, index));
    }

    //true is up or prev, false is down or forwards
    public static void changeQuickSelectPosClient(EntityPlayer player, boolean upOrDown) {
        if (!player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftChangeQuickSelectPos(player, upOrDown));
    }
}
