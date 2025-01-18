package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class RiftPowerRoarMove extends RiftCreatureMove {
    private static final Predicate<EntityLivingBase> ROAR_BLACKLIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = RiftConfigHandler.getConfig(RiftCreatureType.TYRANNOSAURUS).general.affectedByRoarBlacklist;
            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) return entity.isEntityAlive() && !blacklist.contains("minecraft:player");
                else return entity.isEntityAlive() && !blacklist.contains(EntityList.getKey(entity).toString()) && !(entity instanceof RiftEgg);
            }
            else return entity.isEntityAlive() && !(entity instanceof RiftEgg);
        }
    };
    private static final Predicate<EntityLivingBase> ROAR_WHITELIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = RiftConfigHandler.getConfig(RiftCreatureType.TYRANNOSAURUS).general.affectedByRoarBlacklist;

            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) return entity.isEntityAlive() && blacklist.contains("minecraft:player");
                else return entity.isEntityAlive() && blacklist.contains(EntityList.getKey(entity).toString()) && !(entity instanceof RiftEgg);
            }
            else return false;
        }
    };

    public RiftPowerRoarMove() {
        super(CreatureMove.POWER_ROAR, 40, 0.25);
    }

    @Override
    public MovePriority canBeExecuted(RiftCreature user, EntityLivingBase target) {
        if (user.world.rand.nextInt(8) == 0 && user.recentlyHit) return MovePriority.HIGH;
        return MovePriority.NONE;
    }

    @Override
    public void onStartExecuting(RiftCreature user) {
        user.setRoaring(true);
        user.removeSpeed();
    }

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, EntityLivingBase target) {
        this.roar(user, 1.5f);
        user.playSound(RiftSounds.TYRANNOSAURUS_ROAR, 2, 1);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setRoaring(false);
        user.removeSpeed();
    }

    @Override
    public void onHitEntity(RiftCreature user, EntityLivingBase target) {

    }

    @Override
    public void onHitBlock(RiftCreature user, BlockPos targetPos) {

    }

    private void roar(RiftCreature roarUser, float strength) {
        Predicate<EntityLivingBase> targetPredicate = RiftConfigHandler.getConfig(roarUser.creatureType).general.useRoarBlacklistAsWhitelist ? ROAR_WHITELIST : ROAR_BLACKLIST;
        for (EntityLivingBase entity : roarUser.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getRoarArea(roarUser, (double)strength * 6d), targetPredicate)) {
            if (entity != roarUser && RiftUtil.checkForNoAssociations(roarUser, entity)) {
                entity.attackEntityFrom(DamageSource.causeMobDamage(roarUser), 2f);
                this.roarKnockback(roarUser, entity, strength);
            }
        }
        this.roarBreakBlocks(roarUser, strength);
    }

    protected AxisAlignedBB getRoarArea(RiftCreature roarUser, double targetDistance) {
        return roarUser.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    private void roarKnockback(RiftCreature roarUser, EntityLivingBase target, float strength) {
        double d0 = roarUser.posX - target.posX;
        double d1 = roarUser.posZ - target.posZ;
        double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
        target.knockBack(roarUser, strength, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
    }

    private void roarBreakBlocks(RiftCreature roarUser, float strength) {
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
                                float f = (strength * 4) * (0.7F + roarUser.world.rand.nextFloat() * 0.6F);
                                double d4 = roarUser.posX;
                                double d6 = roarUser.posY;
                                double d8 = roarUser.posZ;

                                for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                    BlockPos blockpos = new BlockPos(d4, d6, d8);
                                    IBlockState iblockstate = roarUser.world.getBlockState(blockpos);
                                    Block block = iblockstate.getBlock();

                                    if (iblockstate.getMaterial() != Material.AIR) {
                                        if (roarUser.checkBasedOnStrength(iblockstate)) f -= 0.24F;
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
