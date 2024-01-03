package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IPackHunter;
import com.google.common.base.Predicate;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class RiftControlledPackBuff extends EntityAIBase {
    private final RiftCreature creature;
    private int animTick;
    protected final int animTime;
    private List<RiftCreature> packMembers;

    public RiftControlledPackBuff(RiftCreature creature, float animTimeIn) {
        this.creature = creature;
        this.animTime = (int)(animTimeIn * 20);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isTamed()) {
            UUID ownerID =  this.creature.getOwnerId();
            this.packMembers = this.creature.world.getEntitiesWithinAABB(this.creature.getClass(), this.creature.getHerdBoundingBox(), new Predicate<RiftCreature>() {
                @Override
                public boolean apply(@Nullable RiftCreature input) {
                    if (input.isTamed()) {
                        return ownerID.equals(input.getOwnerId());
                    }
                    return false;
                }
            });
            return this.creature.isBeingRidden() && ((IPackHunter)this.creature).isPackBuffing() && this.creature.getRightClickCooldown() == 0;
        }
        return false;
    }

    @Override
    public void startExecuting() {
        this.animTick = 0;
        this.creature.removeSpeed();
        for (RiftCreature entity : packMembers) {
            for (PotionEffect effect : ((IPackHunter)this.creature).packBuffEffect()) {
                entity.addPotionEffect(effect);
            }
        }
        for (PotionEffect effect : ((IPackHunter)this.creature).packBuffEffect()) {
            this.creature.addPotionEffect(effect);
        }
        this.creature.playSound(RiftSounds.UTAHRAPTOR_CALL, 2, 1);
    }

    public boolean shouldContinueExecuting() {
        return this.animTick <= this.animTime;
    }

    public void resetTask() {
        ((IPackHunter)this.creature).setPackBuffing(false);
        this.creature.setRightClickCooldown(3600);
        this.creature.resetSpeed();
        for (RiftCreature entity : packMembers) entity.resetSpeed();
    }

    public void updateTask() {
        this.animTick++;
    }
}
