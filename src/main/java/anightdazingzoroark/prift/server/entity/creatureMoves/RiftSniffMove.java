package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.MobSize;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSpawnChestDetectParticle;
import anightdazingzoroark.prift.server.message.RiftSpawnDetectParticle;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class RiftSniffMove extends RiftCreatureMove {
    public RiftSniffMove() {
        super(CreatureMove.SNIFF);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return false;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {

    }

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        //for all entities nearby (except those that are submerged)
        int mobSniffRange = RiftConfigHandler.getConfig(user.creatureType).general.mobSniffRange;
        AxisAlignedBB mobDetectAABB = new AxisAlignedBB(user.posX - mobSniffRange, user.posY - mobSniffRange, user.posZ - mobSniffRange, user.posX + mobSniffRange, user.posY + mobSniffRange, user.posZ + mobSniffRange);
        for (EntityLivingBase entityLivingBase : user.world.getEntitiesWithinAABB(EntityLivingBase.class, mobDetectAABB, null)) {
            if (entityLivingBase != user && entityLivingBase != user.getOwner() && !RiftUtil.entityIsUnderwater(entityLivingBase) && RiftUtil.isAppropriateSize(entityLivingBase, MobSize.safeValueOf(RiftConfigHandler.getConfig(user.creatureType).general.maximumMobSniffSize))) {
                RiftMessages.WRAPPER.sendToAll(new RiftSpawnDetectParticle((EntityPlayer)user.getControllingPassenger(), (int)entityLivingBase.posX, (int)entityLivingBase.posY, (int)entityLivingBase.posZ));
            }
        }
        //for chests
        int blockSniffRange = RiftConfigHandler.getConfig(user.creatureType).general.blockSniffRange;
        for (int x = -blockSniffRange; x <= blockSniffRange; x++) {
            for (int y = -blockSniffRange; y <= blockSniffRange; y++) {
                for (int z = -blockSniffRange; z <= blockSniffRange; z++) {
                    BlockPos testPos = user.getPosition().add(x, y, z);
                    if (this.isSniffableBlock(user, user.world.getBlockState(testPos))) {
                        RiftMessages.WRAPPER.sendToAll(new RiftSpawnChestDetectParticle((EntityPlayer)user.getControllingPassenger(), testPos.getX(), testPos.getY(), testPos.getZ()));
                    }
                }
            }
        }
    }

    private boolean isSniffableBlock(RiftCreature user, IBlockState blockState) {
        Block block = blockState.getBlock();
        boolean flag = false;
        for (String blockEntry : RiftConfigHandler.getConfig(user.creatureType).general.sniffableBlocks) {
            if (flag) break;
            int blockIdFirst = blockEntry.indexOf(":");
            int blockIdSecond = blockEntry.indexOf(":", blockIdFirst + 1);
            int blockData = Integer.parseInt(blockEntry.substring(blockIdSecond + 1));
            flag = Block.getBlockFromName(blockEntry.substring(0, blockIdSecond)).equals(block) && (blockData == -1 || block.getMetaFromState(blockState) == blockData);
        }
        return flag;
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
