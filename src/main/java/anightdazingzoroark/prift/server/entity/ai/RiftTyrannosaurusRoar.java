package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.server.entity.creature.Tyrannosaurus;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.Random;

public class RiftTyrannosaurusRoar extends EntityAIBase {
    private final Tyrannosaurus creature;
    private int roarTick;
    private int failAttemptTimer;

    public RiftTyrannosaurusRoar(Tyrannosaurus mob) {
        this.creature = mob;
        this.roarTick = 0;
        this.failAttemptTimer = 0;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.failAttemptTimer <= 0) {
            if (this.creature.hurtTime > 0) {
                int randVal = new Random().nextInt(2);
                if (randVal == 0) {
                    if (!this.creature.isTamed()) {
                        return !this.creature.isActing() && this.creature.canRoar();
                    }
                    else return this.creature.getPassengers().isEmpty()
                            && !this.creature.isActing()
                            && this.creature.canRoar()
                            && !this.creature.isSitting()
                            && this.creature.getTameBehavior() != TameBehaviorType.PASSIVE;
                }
                else {
                    this.failAttemptTimer = 10;
                    return false;
                }
            }
            else return false;
        }
        else {
            this.failAttemptTimer--;
            return false;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.roarTick <= 40 && this.creature.isEntityAlive();
    }

    @Override
    public void startExecuting() {
        this.creature.setRoaring(true);
        this.creature.removeSpeed();
        this.failAttemptTimer = 0;
    }

    @Override
    public void resetTask() {
        this.roarTick = 0;
        this.creature.setRoaring(false);
        this.creature.setCanRoar(false);
        this.creature.resetSpeed();
        if (this.creature.isTamed()) this.creature.roarCharge = 0;
    }

    @Override
    public void updateTask() {
        this.roarTick++;
        if (this.roarTick == 10 && this.creature.isEntityAlive()) {
            this.creature.roar(0.015f * this.creature.roarCharge + 1.5f);
            if (this.creature.isTamed()) this.creature.setEnergy(this.creature.getEnergy() - (int)(0.06d * (double)this.creature.roarCharge + 6d));
            this.creature.playSound(RiftSounds.TYRANNOSAURUS_ROAR, 2, 1);
        }
    }
}
