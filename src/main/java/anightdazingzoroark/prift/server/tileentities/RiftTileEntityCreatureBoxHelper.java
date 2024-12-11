package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftUpdateBoxDeployed;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RiftTileEntityCreatureBoxHelper {
    public static void updateAllDeployedCreatures(World world, BlockPos pos) {
        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);

        if (creatureBox == null) return;

        for (RiftCreature creature : creatureBox.getCreatures()) updateDeployedCreature(pos, creature);
    }

    public static void updateDeployedCreature(BlockPos pos, RiftCreature creature) {
        if (pos == null) return;

        RiftMessages.WRAPPER.sendToServer(new RiftUpdateBoxDeployed(pos, creature));
    }
}