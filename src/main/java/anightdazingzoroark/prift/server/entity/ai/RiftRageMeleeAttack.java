package anightdazingzoroark.prift.server.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

//this class is only to be added to creatures with the rage effect
//that don't have any other attack goals
public class RiftRageMeleeAttack extends EntityAIAttackMelee {
    public RiftRageMeleeAttack(EntityCreature creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
    }

    @Override
    protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
        double d0 = this.getAttackReachSqr(enemy);

        if (distToEnemySqr <= d0 && this.attackTick <= 0) {
            this.attackTick = 20;
            this.attacker.swingArm(EnumHand.MAIN_HAND);

            //attack enemy
            enemy.attackEntityFrom(DamageSource.causeMobDamage(this.attacker), (float)((int)this.attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
        }
    }
}
