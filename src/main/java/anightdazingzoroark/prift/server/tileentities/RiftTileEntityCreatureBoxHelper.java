package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.message.RiftForceUpdateCreatureBoxDeployed;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RiftTileEntityCreatureBoxHelper {
    public static void forceUpdateCreatureBoxDeployed(EntityPlayer player, BlockPos pos) {
        if (player.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftForceUpdateCreatureBoxDeployed(player, pos));
    }

    public static BlockPos creatureCreatureSpawnPoint(BlockPos creatureBoxPos, World world, RiftCreature creature) {
        for (int i = 0; i < 10; i++) {
            int xSpawnPos = RiftUtil.randomInRange(creatureBoxPos.getX() - 16, creatureBoxPos.getX() + 16);
            int ySpawnPos = RiftUtil.randomInRange(creatureBoxPos.getY() - 8, creatureBoxPos.getY() + 8);
            int zSpawnPos = RiftUtil.randomInRange(creatureBoxPos.getZ() - 16, creatureBoxPos.getZ() + 16);
            BlockPos pos = new BlockPos(xSpawnPos, ySpawnPos, zSpawnPos);
            IBlockState downState = world.getBlockState(pos.down());

            if (creature instanceof RiftWaterCreature) {
                RiftWaterCreature waterCreature = (RiftWaterCreature) creature;
                //spawn amphibious creatures
                if (waterCreature.isAmphibious()) {
                    if ((canFitInArea(world, creature, pos) && downState.getMaterial() != Material.AIR) || entireAreaWater(world, creature, pos)) {
                        return new BlockPos(xSpawnPos, ySpawnPos, zSpawnPos);
                    }
                }
                //spawn aquatic creatures
                else {
                    if (entireAreaWater(world, creature, pos)) {
                        return new BlockPos(xSpawnPos, ySpawnPos, zSpawnPos);
                    }
                }
            }
            else {
                //spawn regular land creatures
                if (canFitInArea(world, creature, pos) && downState.getMaterial() != Material.AIR) {
                    return new BlockPos(xSpawnPos, ySpawnPos, zSpawnPos);
                }
            }
        }
        return null;
    }

    //EVERYTHING BELOW IS NOW DEPRECATED
    @Deprecated
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

    @Deprecated
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