package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.MobSize;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSpawnChestDetectParticle;
import anightdazingzoroark.prift.server.message.RiftSpawnDetectParticle;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class RiftSniffMove extends RiftCreatureMove {
    private EntityLiving targetToAttack;

    public RiftSniffMove() {
        super(CreatureMove.SNIFF);
    }

    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user.isTamed()) {
                return super.canBeExecutedUnmounted(user, target)
                        && user.getAgeInTicks() % 3000 == 0
                        && user.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                        && !user.busyAtWork()
                        && !user.busyAtTurretMode()
                        && user.getTameBehavior().equals(TameBehaviorType.AGGRESSIVE);
        }
        return super.canBeExecutedUnmounted(user, target) && user.getAgeInTicks() % 3000 == 0;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {}

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        //for all entities nearby (except those that are submerged)
        int mobSniffRange = RiftConfigHandler.getConfig(user.creatureType).general.mobSniffRange;
        AxisAlignedBB mobDetectAABB = new AxisAlignedBB(user.posX - mobSniffRange, user.posY - mobSniffRange, user.posZ - mobSniffRange, user.posX + mobSniffRange, user.posY + mobSniffRange, user.posZ + mobSniffRange);
        for (EntityLivingBase entityLivingBase : user.world.getEntitiesWithinAABB(EntityLivingBase.class, mobDetectAABB, null)) {
            if (entityLivingBase != user && entityLivingBase != user.getOwner() && !RiftUtil.entityIsUnderwater(entityLivingBase) && MobSize.valueOf(RiftConfigHandler.getConfig(user.creatureType).general.maximumMobSniffSize).isAppropriateSize(entityLivingBase)) {
                //spawn particle for owner
                if (user.getOwner() != null) RiftMessages.WRAPPER.sendTo(
                        new RiftSpawnDetectParticle((int)entityLivingBase.posX, (int)entityLivingBase.posY, (int)entityLivingBase.posZ),
                        (EntityPlayerMP) user.getOwner()
                );

                //if not mounted, set first creature spotted as target
                if (!user.isBeingRidden()
                        && entityLivingBase instanceof EntityLiving
                        && RiftUtil.checkForNoAssociations(user, entityLivingBase)
                        && RiftUtil.checkForNoHerdAssociations(user, entityLivingBase)) {
                    this.targetToAttack = (EntityLiving) entityLivingBase;
                }
            }
        }

        //for chests, only for when it has an owner (aka its tamed)
        if (user.isTamed()) {
            int blockSniffRange = RiftConfigHandler.getConfig(user.creatureType).general.blockSniffRange;
            for (int x = -blockSniffRange; x <= blockSniffRange; x++) {
                for (int y = -blockSniffRange; y <= blockSniffRange; y++) {
                    for (int z = -blockSniffRange; z <= blockSniffRange; z++) {
                        BlockPos testPos = user.getPosition().add(x, y, z);
                        if (this.isSniffableBlock(user, user.world.getBlockState(testPos)) && user.getOwner() != null) {
                            RiftMessages.WRAPPER.sendTo(
                                    new RiftSpawnChestDetectParticle(testPos.getX(), testPos.getY(), testPos.getZ()),
                                    (EntityPlayerMP) user.getOwner()
                            );
                        }
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
        if (this.targetToAttack != null && user.getAttackTarget() == null) user.setAttackTarget(this.targetToAttack);
    }
}
