package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.RiftEgg;
import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RiftTyrannosaurusRoar extends EntityAIBase {
    private static final Predicate<EntityLivingBase> ROAR_BLACKLIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = Arrays.asList(RiftConfig.tyrannosaurusRoarTargetBlacklist);
            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) {
                    return entity.isEntityAlive() && !blacklist.contains("minecraft:player");
                }
                else {
                    return entity.isEntityAlive() && !blacklist.contains(EntityList.getKey(entity).toString()) && !(entity instanceof RiftEgg);
                }
            }
            else {
                return entity.isEntityAlive() && !(entity instanceof RiftEgg);
            }
        }
    };
    private static final Predicate<EntityLivingBase> ROAR_WHITELIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = Arrays.asList(RiftConfig.tyrannosaurusRoarTargetBlacklist);

            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) {
                    return entity.isEntityAlive() && blacklist.contains("minecraft:player");
                }
                else {
                    return entity.isEntityAlive() && blacklist.contains(EntityList.getKey(entity).toString()) && !(entity instanceof RiftEgg);
                }
            }
            else {
                return false;
            }
        }
    };
    protected final Tyrannosaurus mob;
    private int roarTick;

    public RiftTyrannosaurusRoar(Tyrannosaurus mob) {
        this.mob = mob;
        this.roarTick = 0;
    }

    @Override
    public boolean shouldExecute() {
        if (!this.mob.isTamed()) {
            return this.mob.hurtTime > 0 && new Random().nextInt(4) == 0 && this.mob.canRoar();
        }
        else {
            return this.mob.canRoar() && this.mob.isRoaring();
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.roarTick <= 40 && this.mob.isEntityAlive();
    }

    @Override
    public void startExecuting() {
        if (!this.mob.isTamed()) this.mob.setRoaring(true);
    }

    @Override
    public void resetTask() {
        this.roarTick = 0;
        this.mob.setRoaring(false);
        this.mob.setCanRoar(false);
        if (this.mob.isTamed()) {
            this.mob.setCanBeSteered(true);
            this.mob.setActing(false);
            this.mob.roarCharge = 0;
        }
    }

    @Override
    public void updateTask() {
        this.roarTick++;
        if (this.roarTick == 10 && this.mob.isEntityAlive()) {
            this.mob.roar(0.0075f * this.mob.roarCharge + 1.5f);
        }
        if (this.mob.isTamed()) {
            this.mob.setCanBeSteered(false);
        }
    }
}
