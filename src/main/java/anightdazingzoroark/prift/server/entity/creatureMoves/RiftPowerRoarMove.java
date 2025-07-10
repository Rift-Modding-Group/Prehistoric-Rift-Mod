package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class RiftPowerRoarMove extends RiftCreatureMove {
    public RiftPowerRoarMove() {
        super(CreatureMove.POWER_ROAR);
    }

    /*
    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return user.world.rand.nextInt(4) == 0;
    }
     */

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
        this.roar(user,  RiftUtil.slopeResult(useAmount, true, 0, this.creatureMove.maxUse, 1D, 2D));
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }

    private void roar(RiftCreature roarUser, double strength) {
        for (EntityLivingBase entity : roarUser.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getRoarArea(roarUser, strength * 6d), this.generalEntityPredicate(roarUser))) {
            if (entity != roarUser) {
                entity.attackEntityFrom(DamageSource.causeMobDamage(roarUser), 2);
                if (RiftUtil.isAppropriateSizeNotEqual(entity, RiftUtil.getMobSize(roarUser))) this.roarKnockback(roarUser, entity, strength);
            }
        }
        this.roarBreakBlocks(roarUser, strength);
    }

    protected AxisAlignedBB getRoarArea(RiftCreature roarUser, double targetDistance) {
        return roarUser.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    private void roarKnockback(RiftCreature roarUser, EntityLivingBase target, double strength) {
        double d0 = roarUser.posX - target.posX;
        double d1 = roarUser.posZ - target.posZ;
        double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
        target.knockBack(roarUser, (float) strength, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
    }

    private void roarBreakBlocks(RiftCreature roarUser, double strength) {
        List<BlockPos> affectedBlockPositions = Lists.<BlockPos>newArrayList();
        boolean canBreak = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(roarUser.world, roarUser);
        if (canBreak) {
            if (!roarUser.world.isRemote) {
                Set<BlockPos> set = Sets.<BlockPos>newHashSet();
                for (int j = 0; j < 16; ++j) {
                    for (int k = 0; k < 16; ++k) {
                        for (int l = 0; l < 16; ++l) {
                            if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                                double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                                double d1 = Math.abs((double)((float)k / 15.0F * 2.0F - 1.0F));
                                double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
                                double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                                d0 = d0 / d3;
                                d1 = d1 / d3;
                                d2 = d2 / d3;
                                float f = ((float) strength * 4) * (0.7f + roarUser.world.rand.nextFloat() * 0.6f);
                                double d4 = roarUser.posX;
                                double d6 = roarUser.posY;
                                double d8 = roarUser.posZ;

                                for (float f1 = 0.3f; f > 0.0f; f -= 0.22500001f) {
                                    BlockPos blockpos = new BlockPos(d4, d6, d8);
                                    IBlockState iblockstate = roarUser.world.getBlockState(blockpos);

                                    if (iblockstate.getMaterial() != Material.AIR) {
                                        if (roarUser.checkIfCanBreakBlock(iblockstate)) f -= 0.24F;
                                        else f -= (1200F + 0.3F) * 0.3F;

                                        if (f > 0.0F) set.add(blockpos);
                                    }

                                    d4 += d0 * 0.30000001192092896D;
                                    d6 += d1 * 0.30000001192092896D;
                                    d8 += d2 * 0.30000001192092896D;
                                }
                            }
                        }
                    }
                }
                affectedBlockPositions.addAll(set);
                for (BlockPos blockPos : affectedBlockPositions) roarUser.world.destroyBlock(blockPos, false);
            }
        }
    }
}
