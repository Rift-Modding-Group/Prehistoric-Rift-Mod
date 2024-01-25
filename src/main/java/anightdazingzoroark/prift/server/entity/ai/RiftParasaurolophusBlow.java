package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.server.entity.creature.Parasaurolophus;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftParasaurolophusBlow extends EntityAIBase {
    private Parasaurolophus parasaurolophus;
    private final int animLength;
    private final int animBlowTime;
    private int animTime;

    public RiftParasaurolophusBlow(Parasaurolophus parasaurolophus) {
        this.parasaurolophus = parasaurolophus;
        this.animLength = (int)(1.76D * 20D);
        this.animBlowTime = (int)(0.48D * 20D);
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.parasaurolophus.getAttackTarget();
        if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else {
            return this.parasaurolophus.getDistance(entitylivingbase) <= 8f && !this.parasaurolophus.isBeingRidden();
        }
    }

    public void startExecuting() {
        this.parasaurolophus.getLookHelper().setLookPositionWithEntity(this.parasaurolophus.getAttackTarget(), 30.0F, 30.0F);
        this.parasaurolophus.setBlowing(true);
        this.parasaurolophus.removeSpeed();
        this.animTime = 0;
    }

    @Override
    public void resetTask() {
        this.parasaurolophus.setBlowing(false);
        this.parasaurolophus.setAttackTarget(null);
        this.parasaurolophus.setRevengeTarget(null);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.animTime < this.animLength && this.parasaurolophus.isEntityAlive() && !this.parasaurolophus.isBeingRidden() && this.parasaurolophus.canBlow();
    }

    @Override
    public void updateTask() {
        this.animTime++;
        EntityLivingBase entitylivingbase = this.parasaurolophus.getAttackTarget();
        if (entitylivingbase != null) {
            this.parasaurolophus.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
            if (this.animTime == this.animBlowTime) {
                this.parasaurolophus.useBlow(null,4);
                this.parasaurolophus.playSound(RiftSounds.PARASAUROLOPHUS_BLOW, 2, 1);
            }
        }
    }
}