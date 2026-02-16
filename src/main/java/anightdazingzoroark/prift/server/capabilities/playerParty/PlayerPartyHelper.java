package anightdazingzoroark.prift.server.capabilities.playerParty;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.message.NewRiftDeployPartyMem;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerPartyHelper {
    public static final int maxSize = 6;

    public static IPlayerParty getPlayerParty(EntityPlayer player) {
        if (player == null) return null;
        return player.getCapability(PlayerPartyProvider.PLAYER_PARTY_CAPABILITY, null);
    }

    public static void addCreatureToParty(EntityPlayer player, RiftCreature creature) {
        if (player == null || player.world.isRemote) return;
        getPlayerParty(player).addPartyMember(creature);
    }

    public static void updatePartyCreature(EntityPlayer player, RiftCreature creature) {
        if (player == null || player.world.isRemote) return;
        getPlayerParty(player).updatePartyMember(creature);
    }

    public static boolean canAddToParty(EntityPlayer player) {
        if (player == null) return false;
        return getPlayerParty(player).canAddToParty();
    }

    public static boolean canBeDeployed(EntityPlayer player, CreatureNBT creatureNBT) {
        if (player == null || creatureNBT == null || creatureNBT.nbtIsEmpty()) return false;
        //this is temporary, it will be completely replaced when i redo creature registries w something even better :tm:
        boolean isAquatic = creatureNBT.getCreatureType().getCreature().isAssignableFrom(RiftWaterCreature.class);
        if (isAquatic) {
            if (player.world.getBlockState(player.getPosition()).getMaterial() == Material.WATER) return true;
            else if (player.world.getBlockState(player.getPosition().down()).getMaterial() != Material.AIR
                /*&& ((RiftWaterCreature) creature).isAmphibious()*/) return true;
        }
        else {
            if (player.world.getBlockState(player.getPosition().down()).getMaterial() == Material.AIR
                    && player.isRiding()) return true;
            else if (player.world.getBlockState(player.getPosition().down()).getMaterial() != Material.AIR) return true;
        }
        return false;
    }

    public static void deployCreatureFromParty(EntityPlayer player, int index, boolean deploy) {
        if (player == null || index < 0 || index >= maxSize) return;
        RiftMessages.WRAPPER.sendToServer(new NewRiftDeployPartyMem(player, index, deploy));
    }
}
