package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Saurophaganax;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.List;

public class RiftSaurophaganaxUseLightBlast extends EntityAIBase {
    protected final Saurophaganax saurophaganax;
    protected int animTime;
    protected final int blastAttackTime = 19;
    protected final int blastAnimLength = 41;

    public RiftSaurophaganaxUseLightBlast(Saurophaganax saurophaganax) {
        this.saurophaganax = saurophaganax;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return this.saurophaganax.lightBlastCharge() >= 10 && !this.saurophaganax.isBeingRidden();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.animTime <= blastAnimLength;
    }

    @Override
    public void startExecuting() {
        this.animTime = 0;
        this.saurophaganax.setUsingLightBlast(true);
        this.saurophaganax.removeSpeed();
    }

    @Override
    public void resetTask() {
        this.saurophaganax.setUsingLightBlast(false);
        this.saurophaganax.resetSpeed();
        this.saurophaganax.setLightBlastCharge(0);
    }

    @Override
    public void updateTask() {
        this.animTime++;
        if (this.animTime == this.blastAttackTime) {
            List<EntityLivingBase> list = this.saurophaganax.world.getEntitiesWithinAABB(EntityLivingBase.class, this.blastAABB(), new Predicate<EntityLivingBase>() {
                @Override
                public boolean apply(@Nullable EntityLivingBase entityLivingBase) {
                    if (entityLivingBase instanceof EntityPlayer) return saurophaganax.getTargetList().contains("minecraft:player");
                    else return saurophaganax.getTargetList().contains(EntityList.getKey(entityLivingBase).toString());
                }
            });
            for (EntityLivingBase entityT : list) this.saurophaganax.attackWithLightBlast(entityT);
        }
    }

    private AxisAlignedBB blastAABB() {
        return this.saurophaganax.getEntityBoundingBox().grow(6D);
    }
}
