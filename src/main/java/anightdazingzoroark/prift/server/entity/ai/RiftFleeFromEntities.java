package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftFleeFromEntities extends EntityAIBase {
    private final RiftCreature creature;

    public RiftFleeFromEntities(RiftCreature creature) {
        this.creature = creature;
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isTamed()) return false;

        boolean isHerdLeader = this.creature instanceof IHerder ? ((IHerder)this.creature).isHerdLeader() : false;
        boolean isStrayFromHerd = this.creature instanceof IHerder ? !((IHerder)this.creature).isHerdLeader() && !((IHerder)this.creature).hasHerdLeader() : true;

        return this.creature.fleesFromDanger()
                && ((this.creature.getAttackTarget() != null && RiftUtil.checkForNoAssociations(this.creature, this.creature.getAttackTarget()))
                    || (this.creature.getRevengeTarget() != null && RiftUtil.checkForNoAssociations(this.creature, this.creature.getRevengeTarget())))
                && (isHerdLeader || isStrayFromHerd);
    }
}
