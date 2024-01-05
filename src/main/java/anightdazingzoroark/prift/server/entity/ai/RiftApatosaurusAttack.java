package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.Apatosaurus;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;

public class RiftApatosaurusAttack extends RiftAttack {
    protected int whipAnimLength;
    protected int whipAnimTime;
    private int attackMode; //0 is stomp, 1 is tail whip

    //apato has two attacks, stomp and tail whip
    //the default attack stuff is for the stomp
    public RiftApatosaurusAttack(RiftCreature creature, double speedIn, float attackAnimLength, float attackAnimTime) {
        super(creature, speedIn, attackAnimLength, attackAnimTime);
        this.whipAnimLength = (int)(0.6f * 20f);
        this.whipAnimTime = (int)(0.4f * 20f);
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        this.attackMode = RiftUtil.randomInRange(0, 1);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        ((Apatosaurus)(this.attacker)).setTailWhipping(false);
    }

    @Override
    protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
        double d0 = this.getAttackReachSqr(enemy);

        if (--this.attackCooldown <= 0) {
            this.animTime++;

            if (this.attackMode == 0) {
                if (distToEnemySqr <= d0) this.attacker.setAttacking(true);
                if (this.animTime == this.attackAnimTime) {
                    if (distToEnemySqr <= d0) this.attacker.attackEntityAsMob(enemy);
                }
                if (this.animTime > this.attackAnimLength + 1) {
                    this.animTime = 0;
                    this.attacker.setAttacking(false);
                    this.attackCooldown = 20;
                    this.attackMode = RiftUtil.randomInRange(0, 1);
                }
            }
            else if (this.attackMode == 1) {
                Apatosaurus apatosaurus = (Apatosaurus) this.attacker;
                if (distToEnemySqr <= d0) apatosaurus.setTailWhipping(true);
                if (this.animTime == this.whipAnimTime) {
                    if (distToEnemySqr <= d0) apatosaurus.useWhipAttack();
                }
                if (this.animTime > this.whipAnimLength) {
                    this.animTime = 0;
                    apatosaurus.setTailWhipping(false);
                    this.attackCooldown = 20;
                    this.attackMode = RiftUtil.randomInRange(0, 1);
                }
            }
        }
    }
}
