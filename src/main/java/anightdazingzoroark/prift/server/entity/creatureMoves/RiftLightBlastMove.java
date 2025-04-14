package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;
import java.util.List;

public class RiftLightBlastMove extends RiftCreatureMove {
    public RiftLightBlastMove() {
        super(CreatureMove.LIGHT_BLAST);
    }

    @Override
    public String cannotExecuteMountedMessage() {
        return "reminder.insufficient_light_blast_charge";
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.removeSpeed();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {

    }

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        List<EntityLivingBase> list = user.world.getEntitiesWithinAABB(EntityLivingBase.class, user.getEntityBoundingBox().grow(8D), new Predicate<EntityLivingBase>() {
            @Override
            public boolean apply(@Nullable EntityLivingBase entityLivingBase) {
                return RiftUtil.checkForNoAssociations(user, entityLivingBase);
            }
        });
        for (EntityLivingBase entity : list) {
            entity.attackEntityFrom(DamageSource.causeMobDamage(user), (float)((int)user.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue())/4F);
            entity.setFire(30);
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
    }
}
