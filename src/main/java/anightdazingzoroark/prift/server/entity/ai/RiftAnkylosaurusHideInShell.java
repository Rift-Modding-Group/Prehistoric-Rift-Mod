package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Ankylosaurus;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftAnkylosaurusHideInShell extends EntityAIBase {
    private final Ankylosaurus ankylosaurus;
    private int animTick;
    private boolean exitFlag;
    private int healTick;

    public RiftAnkylosaurusHideInShell(Ankylosaurus ankylosaurus) {
        this.ankylosaurus = ankylosaurus;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        float healthRatio = this.ankylosaurus.getHealth()/this.ankylosaurus.getMaxHealth();
        return (healthRatio <= 0.25 || this.ankylosaurus.getForceShellFlag()) && !this.ankylosaurus.isBaby();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return (this.ankylosaurus.isStartHiding() || this.ankylosaurus.isHiding() || this.ankylosaurus.isStopHiding()) && this.ankylosaurus.isEntityAlive();
    }

    @Override
    public void startExecuting() {
        this.animTick = 0;
        this.healTick = 0;
        this.ankylosaurus.setStartHiding(true);
        this.ankylosaurus.getNavigator().clearPath();
        this.ankylosaurus.removeSpeed();
        this.exitFlag = true;
        this.ankylosaurus.setForceShellFlag(false);
    }

    @Override
    public void resetTask() {
        this.animTick = 0;
        this.healTick = 0;
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
            //regenerate in shell
            this.healTick++;
            if (this.healTick >= 60) {
                this.ankylosaurus.heal(2f);
                this.healTick = 0;
            }
        }
        if (this.ankylosaurus.isStopHiding()) {
            this.animTick++;
            if (this.animTick >= 21) {
                this.ankylosaurus.setStopHiding(false);
                this.animTick = 0;
            }
        }
        if (!this.ankylosaurus.isStopHiding() && ((healthRatio >= 0.5 && !this.ankylosaurus.isBeingRidden() && this.exitFlag) || this.ankylosaurus.getForceShellFlag())) {
            this.ankylosaurus.setStopHiding(true);
            this.ankylosaurus.setHiding(false);
            this.ankylosaurus.setStartHiding(false);
            this.ankylosaurus.resetSpeed();
            this.exitFlag = false;
            this.ankylosaurus.setForceShellFlag(false);
        }
    }
}
