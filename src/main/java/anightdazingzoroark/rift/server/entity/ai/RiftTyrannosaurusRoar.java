package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.server.RiftUtil;
import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.terraingen.OreGenEvent;
import org.lwjgl.Sys;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static anightdazingzoroark.rift.server.RiftUtil.removeElementFromArray;

public class RiftTyrannosaurusRoar extends EntityAIBase {
    private static final Predicate<EntityLiving> ROAR_BLACKLIST = new Predicate<EntityLiving>() {
        @Override
        public boolean apply(@Nullable EntityLiving entity) {
            String[] blacklist = RiftConfig.tyrannosaurusRoarTargetBlacklist;
            blacklist = removeElementFromArray(blacklist, "minecraft:player");
            return entity.isEntityAlive() && !Arrays.asList(blacklist).contains(EntityList.getKey(entity).toString());
        }
    };
    private static final Predicate<EntityLiving> ROAR_WHITELIST = new Predicate<EntityLiving>() {
        @Override
        public boolean apply(@Nullable EntityLiving entity) {
            String[] blacklist = RiftConfig.tyrannosaurusRoarTargetBlacklist;
            blacklist = removeElementFromArray(blacklist, "minecraft:player");
            return entity.isEntityAlive() && Arrays.asList(blacklist).contains(EntityList.getKey(entity).toString());
        }
    };
    protected final Tyrannosaurus mob;
    private int useTick;
    private int roarTick;

    public RiftTyrannosaurusRoar(Tyrannosaurus mob) {
        this.mob = mob;
        this.useTick = 0;
        this.roarTick = 0;
    }

    @Override
    public boolean shouldExecute() {
        return this.mob.hurtTime > 0 && new Random().nextInt(4) == 0 && this.mob.canRoar();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.roarTick <= 40 && this.mob.isEntityAlive();
    }

    @Override
    public void startExecuting() {
        this.mob.setRoaring(true);
    }

    @Override
    public void resetTask() {
        this.roarTick = 0;
        this.mob.setRoaring(false);
        this.mob.setCanRoar(false);
    }

    @Override
    public void updateTask() {
        this.roarTick++;
        if (this.roarTick == 10 && this.mob.isEntityAlive()) {
            Predicate<EntityLiving> targetPredicate = RiftConfig.tyrannosaurusRoarTargetsWhitelist ? ROAR_WHITELIST : ROAR_BLACKLIST;
            for (Entity entity : this.mob.world.getEntitiesWithinAABB(EntityLiving.class, this.getTargetableArea(12.0D), targetPredicate)) {
                if (entity != this.mob) {
                    entity.attackEntityFrom(DamageSource.causeMobDamage(this.mob), 2f);
                    this.strongKnockback(entity);
                }
            }
            if ((Arrays.asList(RiftConfig.tyrannosaurusRoarTargetBlacklist).contains("minecraft:player") && RiftConfig.tyrannosaurusRoarTargetsWhitelist) || (!Arrays.asList(RiftConfig.tyrannosaurusRoarTargetBlacklist).contains("minecraft:player") && !RiftConfig.tyrannosaurusRoarTargetsWhitelist)) {
                for (EntityPlayer entity : this.mob.world.getEntitiesWithinAABB(EntityPlayer.class, this.getTargetableArea(12.0D), null)) {
                    entity.attackEntityFrom(DamageSource.causeMobDamage(this.mob), 2f);
                    this.strongKnockback(entity);
                }
            }
            this.breakBlocks();
        }
    }

    private void strongKnockback(Entity target) {
        double d0 = this.mob.posX - target.posX;
        double d1 = this.mob.posZ - target.posZ;
        double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
        ((EntityLivingBase)target).knockBack(this.mob, 1.5f, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.mob.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    private void breakBlocks() {
        List<BlockPos> affectedBlockPositions = Lists.<BlockPos>newArrayList();
        boolean canBreak = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.mob.world, this.mob);
        if (canBreak) {
            if (!this.mob.world.isRemote) {
                Set<BlockPos> set = Sets.<BlockPos>newHashSet();
                int i = 16;
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
                                float f = 8f * (0.7F + this.mob.world.rand.nextFloat() * 0.6F);
                                double d4 = this.mob.posX;
                                double d6 = this.mob.posY;
                                double d8 = this.mob.posZ;

                                for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                    BlockPos blockpos = new BlockPos(d4, d6, d8);
                                    IBlockState iblockstate = this.mob.world.getBlockState(blockpos);
                                    Block block = iblockstate.getBlock();

                                    if (iblockstate.getMaterial() != Material.AIR) {
                                        if (RiftUtil.blockWeakerThanWood(block)) {
                                            f -= 0.24F;
                                        }
                                        else {
                                            f -= (1200F + 0.3F) * 0.3F;
                                        }

                                        if (f > 0.0F) {
                                            set.add(blockpos);
                                        }
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
                for (BlockPos blockPos : affectedBlockPositions) {
                    this.mob.world.destroyBlock(blockPos, true);
                }
            }
        }
    }
}
