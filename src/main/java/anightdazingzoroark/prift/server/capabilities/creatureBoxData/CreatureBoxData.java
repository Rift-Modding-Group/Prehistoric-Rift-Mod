package anightdazingzoroark.prift.server.capabilities.creatureBoxData;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CreatureBoxData implements ICreatureBoxData {
    public static final int maximumCreatureBoxesPerPlayer = 5;
    private List<CreatureBoxInfo> creatureBoxPositions = new ArrayList<>();

    @Override
    public List<CreatureBoxInfo> getCreatureBoxInformation() {
        return this.creatureBoxPositions;
    }

    @Override
    public List<BlockPos> getCreatureBoxPositionsByPlayer(EntityPlayer player) {
        return this.creatureBoxPositions.stream()
        .filter(info -> info.ownerUUID.equals(player.getUniqueID()))
        .map(info -> {
                return info.creatureBoxPos;
            }
        ).collect(Collectors.toList());
    }

    @Override
    public void removeByPosition(BlockPos pos) {
        this.creatureBoxPositions = this.creatureBoxPositions.stream().filter(
            info -> !info.creatureBoxPos.equals(pos)
        ).collect(Collectors.toList());
    }

    @Override
    public RiftTileEntityCreatureBox getTileEntityByUUID(World world, UUID uuid) {
        if (uuid == null || uuid.equals(RiftUtil.nilUUID)) return null;
        for (CreatureBoxInfo info : this.creatureBoxPositions) {
            BlockPos pos = info.creatureBoxPos;
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
        for (CreatureBoxInfo info : this.creatureBoxPositions) {
            BlockPos pos = info.creatureBoxPos;
            TileEntity tileEntity = world.getTileEntity(pos);
            if (!(tileEntity instanceof RiftTileEntityCreatureBox)) continue;
            RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) tileEntity;
            if (pos.equals(testPos)) return teCreatureBox;
        }
        return null;
    }
}
