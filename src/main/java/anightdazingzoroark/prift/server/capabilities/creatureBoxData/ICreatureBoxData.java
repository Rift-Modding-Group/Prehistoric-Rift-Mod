package anightdazingzoroark.prift.server.capabilities.creatureBoxData;

import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public interface ICreatureBoxData {
    List<BlockPos> getCreatureBoxPositions();
    RiftTileEntityCreatureBox getTileEntityByUUID(World world, UUID uuid);
    RiftTileEntityCreatureBox getTileEntityByPos(World world, BlockPos testPos);
}
