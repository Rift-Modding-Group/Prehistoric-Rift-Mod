package anightdazingzoroark.prift.server.capabilities.creatureBoxData;

import anightdazingzoroark.prift.server.tileentities.RiftNewTileEntityCreatureBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public interface ICreatureBoxData {
    List<BlockPos> getCreatureBoxPositions();
    RiftNewTileEntityCreatureBox getTileEntityByUUID(World world, UUID uuid);
    RiftNewTileEntityCreatureBox getTileEntityByPos(World world, BlockPos testPos);
}
