package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RiftHurtByTarget extends EntityAIHurtByTarget {
    @NotNull
    private final RiftCreature creature;

    public RiftHurtByTarget(@NotNull RiftCreature creature) {
        super(creature, false);
        this.creature = creature;
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase revengeTarget = this.creature.getRevengeTarget();
        if (revengeTarget == null) return false;

        //block if related to creature
        if (revengeTarget instanceof RiftCreature revengeCreature && this.creature.isRelatedToEntity(revengeCreature)) {
            return false;
        }

        //getRetaliateWhenAttacked check
        if (this.creature.getCreatureType().getRetaliateWhenAttacked() == null || !this.creature.getCreatureType().getRetaliateWhenAttacked().apply(this.creature, revengeTarget)
        ) {
            return false;
        }

        return super.shouldExecute();
    }

    @Override
    public void startExecuting() {
        super.startExecuting();

        //remember target if its a player
        if (this.creature.getCreatureType().getRememberPlayerAttacker()
                && this.creature.getRevengeTarget() instanceof EntityPlayer player
        ) this.creature.rememberPlayerTarget(player);

        //broadcast revenge target to herdmates
        if (this.creature.getHerd() != null) this.creature.getHerd().retaliate(this.creature, this.creature.getRevengeTarget());
    }

    @Override
    public boolean shouldContinueExecuting() {
        //eeeeeeeee
        if (this.creature.getAttackTarget() == null || this.creature.getAttackTarget() != this.target) return false;

        //herder checks (if herder is not leader it cannot target)
        if (this.creature.isInHerd() && !this.creature.isHerdLeader()) {
            RiftCreature herdLeader = this.creature.getHerdLeader();
            if (herdLeader == null || herdLeader.getAttackTarget() == null || this.creature.getAttackTarget() != herdLeader.getAttackTarget()) {
                return false;
            }
        }
        return super.shouldContinueExecuting();
    }

    @Override
    public void resetTask() {
        if (this.creature.isInHerd() && !this.creature.isHerdLeader()) this.target = null;
        else if (this.creature.getAttackTarget() != this.target) this.target = null;
        else super.resetTask();
    }

    @Override
    protected boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles) {
        if (target == null) return false;
        return EntityAITarget.isSuitableTarget(this.taskOwner, target, includeInvincibles, !this.creature.isInHerd())
                && this.taskOwner.isWithinHomeDistanceFromPosition(new BlockPos(target));
    }
}
