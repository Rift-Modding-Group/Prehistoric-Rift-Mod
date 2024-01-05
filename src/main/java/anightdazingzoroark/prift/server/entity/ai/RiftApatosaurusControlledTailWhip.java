package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Apatosaurus;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;

public class RiftApatosaurusControlledTailWhip extends RiftControlledAttack {
    private Apatosaurus apatosaurus;

    public RiftApatosaurusControlledTailWhip(RiftCreature creature, float attackAnimLength, float attackAnimTime) {
        super(creature, attackAnimLength, attackAnimTime);
        this.apatosaurus = (Apatosaurus) creature;
    }

    @Override
    public boolean shouldExecute() {
        return this.apatosaurus.isTamed() && this.apatosaurus.isBeingRidden() && this.apatosaurus.isTailWhipping();
    }

    @Override
    public void resetTask() {
        this.animTime = 0;
        this.apatosaurus.setTailWhipping(false);
    }

    @Override
    public void updateTask() {
        this.animTime++;
        if (this.animTime == this.attackAnimTime) this.apatosaurus.useWhipAttack();
    }
}
