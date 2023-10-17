package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.server.entity.RiftEgg;
import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RiftTyrannosaurusRoar extends EntityAIBase {
    protected final Tyrannosaurus mob;
    private int roarTick;
    private int revengeTimerOld;

    public RiftTyrannosaurusRoar(Tyrannosaurus mob) {
        this.mob = mob;
        this.revengeTimerOld = this.mob.getRevengeTimer();
        this.roarTick = 0;
    }

    @Override
    public boolean shouldExecute() {
        if (!this.mob.isTamed()) {
//            int i = this.mob.getRevengeTimer();
//            EntityLivingBase entitylivingbase = this.mob.getRevengeTarget();
//            return i != this.revengeTimerOld && entitylivingbase != null && entitylivingbase instanceof EntityLivingBase && new Random().nextInt(4) == 0 && this.mob.canRoar();
            return !this.mob.isActing() && this.mob.hurtTime > 0 && new Random().nextInt(4) == 0 && this.mob.canRoar();
        }
        else return this.mob.canRoar() && this.mob.isRoaring();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.roarTick <= 40 && this.mob.isEntityAlive();
    }

    @Override
    public void startExecuting() {
        if (!this.mob.isTamed()) {
            this.mob.setRoaring(true);
            this.revengeTimerOld = this.mob.getRevengeTimer();
        }
        this.mob.removeSpeed();
    }

    @Override
    public void resetTask() {
        this.roarTick = 0;
        this.mob.setRoaring(false);
        this.mob.setCanRoar(false);
        this.mob.resetSpeed();
        if (this.mob.isTamed()) this.mob.roarCharge = 0;
    }

    @Override
    public void updateTask() {
        this.roarTick++;
        if (this.roarTick == 10 && this.mob.isEntityAlive()) {
            this.mob.roar(0.015f * this.mob.roarCharge + 1.5f);
            if (this.mob.isTamed()) this.mob.setEnergy(this.mob.getEnergy() - (int)(0.06d * (double)this.mob.roarCharge + 6d));
        }
    }
}
