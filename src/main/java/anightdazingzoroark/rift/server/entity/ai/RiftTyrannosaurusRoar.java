package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.client.RiftSounds;
import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import anightdazingzoroark.rift.server.enums.TameBehaviorType;
import anightdazingzoroark.rift.server.enums.TameStatusType;
import net.minecraft.entity.ai.EntityAIBase;

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
        else return this.mob.getPassengers().isEmpty() && !this.mob.isActing() && this.mob.hurtTime > 0 && new Random().nextInt(4) == 0 && this.mob.canRoar() && this.mob.getTameStatus() != TameStatusType.SIT && this.mob.getTameBehavior() != TameBehaviorType.PASSIVE;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.roarTick <= 40 && this.mob.isEntityAlive();
    }

    @Override
    public void startExecuting() {
        this.mob.setRoaring(true);
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
            this.mob.playSound(RiftSounds.TYRANNOSAURUS_ROAR, 2, 1);
        }
    }
}
