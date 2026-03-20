package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RiftTileEntityCreatureBoxHelper {
    public static BlockPos creatureCreatureSpawnPoint(BlockPos creatureBoxPos, World world, RiftCreature creature) {
        if (creature == null || creatureBoxPos == null) return null;

        //get creature box
        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(creatureBoxPos);
        if (creatureBox == null) return null;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 100; i++) {
            if (creature instanceof RiftWaterCreature waterCreature) {
                int xSpawnPos = RiftUtil.randomInRange(creatureBox.getXBounds()[0], creatureBox.getXBounds()[1]);
                int ySpawnPos = RiftUtil.randomInRange(creatureBox.getYBounds()[0], creatureBox.getYBounds()[1]);
                int zSpawnPos = RiftUtil.randomInRange(creatureBox.getZBounds()[0], creatureBox.getZBounds()[1]);
                pos.setPos(xSpawnPos, ySpawnPos, zSpawnPos);
                IBlockState downState = world.getBlockState(pos.down());

                //spawn amphibious creatures
                if (waterCreature.isAmphibious()) {
                    if ((canFitInArea(world, creature, pos) && downState.getMaterial() != Material.AIR)
                            || entireAreaWater(world, creature, pos)) {
                        return new BlockPos(pos);
                    }
                }
                //spawn aquatic creatures
                else {
                    if (entireAreaWater(world, creature, pos)) {
                        return new BlockPos(pos);
                    }
                }
            }
            else {
                int xSpawnPos = RiftUtil.randomInRange(creatureBox.getXBounds()[0], creatureBox.getXBounds()[1]);
                int ySpawnPos = RiftUtil.randomInRange(creatureBox.getYBounds()[0], creatureBox.getYBounds()[1]);
                int zSpawnPos = RiftUtil.randomInRange(creatureBox.getZBounds()[0], creatureBox.getZBounds()[1]);
                pos.setPos(xSpawnPos, ySpawnPos, zSpawnPos);
                IBlockState downState = world.getBlockState(pos.down());

                //spawn regular land creatures
                if (canFitInArea(world, creature, pos) && downState.getMaterial() != Material.AIR) {
                    return new BlockPos(pos);
                }
            }
        }
        return null;
    }

    private static boolean canFitInArea(World world, RiftCreature creature, BlockPos pos) {
        int xMin = (int)Math.floor(creature.width / 2);
        for (int x = -xMin; x <= xMin; x++) {
            for (int y = 0; y < (int)Math.ceil(creature.height); y++) {
                for (int z = -xMin; z <= xMin; z++) {
                    BlockPos newPos = pos.add(x, y, z);
                    IBlockState state = world.getBlockState(newPos);
                    if (state.getMaterial() != Material.AIR) return false;
                }
            }
        }
        return true;
    }

    private static boolean entireAreaWater(World world, RiftCreature creature, BlockPos pos) {
        int xMin = (int)Math.floor(creature.width / 2);
        for (int x = -xMin; x <= xMin; x++) {
            for (int y = 0; y < (int)Math.ceil(creature.height); y++) {
                for (int z = -xMin; z <= xMin; z++) {
                    IBlockState state = world.getBlockState(pos.add(x, y, z));
                    if (state.getMaterial() != Material.WATER) return false;
                }
            }
        }
        return true;
    }
}