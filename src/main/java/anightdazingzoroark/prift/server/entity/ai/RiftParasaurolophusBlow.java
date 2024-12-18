package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.server.entity.creature.Parasaurolophus;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;

public class RiftParasaurolophusBlow extends EntityAIBase {
    private Parasaurolophus parasaurolophus;
    private final int animLength;
    private final int animBlowTime;
    private int animTime;
    private Path path;
    private boolean useFlag;
    private boolean startBlowFlag;

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
            if (this.parasaurolophus.isTamed()) {
                this.path = this.parasaurolophus.getNavigator().getPathToEntityLiving(entitylivingbase);
                if (this.path != null) return this.parasaurolophus.getEnergy() > 6 && !this.parasaurolophus.isBeingRidden();
            }
            return this.parasaurolophus.getEnergy() > 6 && this.parasaurolophus.getDistance(entitylivingbase) <= 16f && !this.parasaurolophus.isBeingRidden();
        }
    }

    public void startExecuting() {
        EntityLivingBase entitylivingbase = this.parasaurolophus.getAttackTarget();
        if (entitylivingbase != null) this.parasaurolophus.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
        this.animTime = 0;
        this.useFlag = true;
        this.startBlowFlag = false;
    }

    @Override
    public void resetTask() {
        this.parasaurolophus.setBlowing(false);
        if (!this.parasaurolophus.isTurretMode()) this.parasaurolophus.resetSpeed();
    }

    @Override
    public boolean shouldContinueExecuting() {
        EntityLivingBase entitylivingbase = this.parasaurolophus.getAttackTarget();
        if (entitylivingbase == null) return false;
        else return this.parasaurolophus.getEnergy() > 6 && this.useFlag && entitylivingbase.isEntityAlive() && this.parasaurolophus.isEntityAlive() && !this.parasaurolophus.isBeingRidden() && this.parasaurolophus.canBlow();
    }

    @Override
    public void updateTask() {
        EntityLivingBase entitylivingbase = this.parasaurolophus.getAttackTarget();
        if (entitylivingbase != null) {
            this.parasaurolophus.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
            this.parasaurolophus.getMoveHelper().setMoveTo(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, 1D);

            if (((this.parasaurolophus.isTamed() && this.parasaurolophus.getDistance(entitylivingbase) <= 6) || !this.parasaurolophus.isTamed()) && !this.startBlowFlag) {
                this.startBlowFlag = true;
            }
            if (this.startBlowFlag) {
                if (this.animTime == 0) {
                    this.parasaurolophus.setBlowing(true);
                    this.parasaurolophus.removeSpeed();
                }
                if (this.animTime == this.animBlowTime) {
                    this.parasaurolophus.useBlow(4);
                    this.parasaurolophus.playSound(RiftSounds.PARASAUROLOPHUS_BLOW, 2, 1);
                }
                if (this.animTime > this.animLength) {
                    this.animTime = -1;
                    if (!this.parasaurolophus.isTurretMode()) this.parasaurolophus.resetSpeed();
                    this.parasaurolophus.setBlowing(false);
                    this.startBlowFlag = false;
                    if (!this.parasaurolophus.isTamed()) this.useFlag = false;
                    if (this.parasaurolophus.isTamed()) this.parasaurolophus.setEnergy(this.parasaurolophus.getEnergy() - 3);
                }
                this.animTime++;
            }
        }
    }
}
