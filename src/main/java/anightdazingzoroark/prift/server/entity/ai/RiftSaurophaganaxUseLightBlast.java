package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.client.RiftSounds;
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
        if (!this.saurophaganax.isTamed()) return this.saurophaganax.lightBlastCharge() >= 10 && !this.saurophaganax.isBeingRidden() && this.saurophaganax.getAttackTarget() != null;
        else {
            //when ridden
            if (this.saurophaganax.isBeingRidden()) return this.saurophaganax.isUsingLightBlast();
            //when unridden
            return this.saurophaganax.lightBlastCharge() >= 10 && this.saurophaganax.getEnergy() >= 6 && this.saurophaganax.getAttackTarget() != null;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.animTime <= this.blastAnimLength;
    }

    @Override
    public void startExecuting() {
        this.animTime = 0;
        this.saurophaganax.setUsingLightBlast(true);
        this.saurophaganax.removeSpeed();
        this.saurophaganax.playSound(RiftSounds.SAUROPHAGANAX_LIGHT_BLAST_BOOM, 2, 1);
    }

    @Override
    public void resetTask() {
        this.saurophaganax.setUsingLightBlast(false);
        this.saurophaganax.resetSpeed();
        this.saurophaganax.setLightBlastCharge(0);
        if (this.saurophaganax.isTamed()) this.saurophaganax.setEnergy(this.saurophaganax.getEnergy() - 6);
        if (this.saurophaganax.isTamed() && this.saurophaganax.isBeingRidden()) this.saurophaganax.setRightClickUse(0);
    }

    @Override
    public void updateTask() {
        this.animTime++;
        if (this.animTime == this.blastAttackTime) this.saurophaganax.useLightBlast();
    }
}
