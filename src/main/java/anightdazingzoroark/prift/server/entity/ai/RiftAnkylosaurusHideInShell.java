package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.Ankylosaurus;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;

public class RiftAnkylosaurusHideInShell extends EntityAIBase {
    private final Ankylosaurus ankylosaurus;
    private int animTick;
    private boolean exitFlag;

    public RiftAnkylosaurusHideInShell(Ankylosaurus ankylosaurus) {
        this.ankylosaurus = ankylosaurus;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        float healthRatio = this.ankylosaurus.getHealth()/this.ankylosaurus.getMaxHealth();
        return healthRatio <= 0.25;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.ankylosaurus.isStartHiding() || this.ankylosaurus.isHiding() || this.ankylosaurus.isStopHiding();
    }

    @Override
    public void startExecuting() {
        this.animTick = 0;
        this.ankylosaurus.setStartHiding(true);
        this.ankylosaurus.removeSpeed();
        this.exitFlag = true;
    }

    @Override
    public void resetTask() {
        this.animTick = 0;
        this.ankylosaurus.resetSpeed();
    }

    @Override
    public void updateTask() {
        this.ankylosaurus.setAttackTarget(null);
        float healthRatio = this.ankylosaurus.getHealth()/this.ankylosaurus.getMaxHealth();

        if (this.ankylosaurus.isStartHiding()) {
            this.animTick++;
            if (this.animTick >= 21) {
                this.ankylosaurus.setHiding(true);
                this.ankylosaurus.setStartHiding(false);
                this.ankylosaurus.setStopHiding(false);
                this.animTick = 0;
            }
        }
        if (this.ankylosaurus.isHiding()) {
            for (EntityLivingBase entityLivingBase : this.ankylosaurus.world.getEntitiesWithinAABB(EntityLivingBase.class, this.ankylosaurus.getEntityBoundingBox().grow(2D), new Predicate<EntityLivingBase>() {
                @Override
                public boolean apply(@Nullable EntityLivingBase entityLivingBase) {
                    if (entityLivingBase == null) return false;
                    return !entityLivingBase.equals(ankylosaurus) && RiftUtil.checkForNoAssociations(ankylosaurus, entityLivingBase);
                }
            })) entityLivingBase.attackEntityFrom(DamageSource.causeMobDamage(this.ankylosaurus), 2f);
        }
        if (this.ankylosaurus.isStopHiding()) {
            this.animTick++;
            if (this.animTick >= 21) {
                this.ankylosaurus.setStopHiding(false);
                this.animTick = 0;
            }
        }
        if (healthRatio > 0.25 && !this.ankylosaurus.isStopHiding() && this.exitFlag) {
            this.ankylosaurus.setStopHiding(true);
            this.ankylosaurus.setHiding(false);
            this.ankylosaurus.setStartHiding(false);
            this.ankylosaurus.resetSpeed();
            this.exitFlag = false;
        }
    }
}
