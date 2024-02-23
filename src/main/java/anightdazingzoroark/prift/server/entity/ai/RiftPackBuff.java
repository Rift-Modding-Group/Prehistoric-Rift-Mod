package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IPackHunter;
import com.google.common.base.Predicate;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nullable;
import java.util.List;

public class RiftPackBuff extends EntityAIBase {
    protected final RiftCreature attacker;
    private int animTick;
    protected final int animTime;
    protected final int cooldown;
    private List<RiftCreature> packMembers;

    public RiftPackBuff(RiftCreature attackerIn, float animTimeIn, float cooldownIn) {
        this.attacker = attackerIn;
        this.animTime = (int)(animTimeIn * 20);
        this.cooldown = (int)(cooldownIn * 20);
//        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.attacker.isTamed()) return false;
        else if (this.attacker.getAttackTarget() == null) return false;
        else {
            this.packMembers = this.attacker.world.getEntitiesWithinAABB(this.attacker.getClass(), this.attacker.herdBoundingBox(), new Predicate<RiftCreature>() {
                @Override
                public boolean apply(@Nullable RiftCreature input) {
                    return !input.isTamed();
                }
            });
            this.packMembers.remove(this.attacker);
            return this.attacker.isHerdLeader() && this.packMembers.size() >= 2 && ((IPackHunter)this.attacker).getPackBuffCooldown() == 0;
        }
    }

    @Override
    public void startExecuting() {
        this.animTick = 0;
        ((IPackHunter)this.attacker).setPackBuffing(true);
        this.attacker.removeSpeed();
        for (RiftCreature entity : packMembers) {
            entity.removeSpeed();
            for (PotionEffect effect : ((IPackHunter)this.attacker).packBuffEffect()) {
                entity.addPotionEffect(effect);
            }
        }
        for (PotionEffect effect : ((IPackHunter)this.attacker).packBuffEffect()) {
            this.attacker.addPotionEffect(effect);
        }
        this.attacker.playSound(RiftSounds.UTAHRAPTOR_CALL, 2, 1);
    }

    public boolean shouldContinueExecuting() {
        return this.animTick <= this.animTime;
    }

    public void resetTask() {
        ((IPackHunter)this.attacker).setPackBuffing(false);
        ((IPackHunter)this.attacker).setPackBuffCooldown(this.cooldown);
        this.attacker.resetSpeed();
        for (RiftCreature entity : packMembers) entity.resetSpeed();
    }

    public void updateTask() {
        this.animTick++;
    }
}
