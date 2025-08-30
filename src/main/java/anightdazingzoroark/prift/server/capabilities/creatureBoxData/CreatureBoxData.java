package anightdazingzoroark.prift.server.capabilities.creatureBoxData;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreatureBoxData implements ICreatureBoxData {
    private final List<BlockPos> creatureBoxPositions = new ArrayList<>();

    @Override
    public List<BlockPos> getCreatureBoxPositions() {
        return this.creatureBoxPositions;
    }

    @Override
    public RiftTileEntityCreatureBox getTileEntityByUUID(World world, UUID uuid) {
        if (uuid == null || uuid.equals(RiftUtil.nilUUID)) return null;
        for (BlockPos pos : this.creatureBoxPositions) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (!(tileEntity instanceof RiftTileEntityCreatureBox)) continue;
            RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) tileEntity;
            if (teCreatureBox.getUniqueID().equals(uuid)) return teCreatureBox;
        }
        return null;
    }

    @Override
    public RiftTileEntityCreatureBox getTileEntityByPos(World world, BlockPos testPos) {
        if (testPos == null) return null;
        for (BlockPos pos : this.creatureBoxPositions) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (!(tileEntity instanceof RiftTileEntityCreatureBox)) continue;
            RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) tileEntity;
            if (pos.equals(testPos)) return teCreatureBox;
        }
        return null;
    }
}
