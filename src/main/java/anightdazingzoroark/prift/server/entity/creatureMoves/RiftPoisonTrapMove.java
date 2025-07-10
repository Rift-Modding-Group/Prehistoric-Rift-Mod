package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.other.RiftTrap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;

public class RiftPoisonTrapMove extends RiftCreatureMove {
    public RiftPoisonTrapMove() {
        super(CreatureMove.POISON_TRAP);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return !user.isTamed();
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {}

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        RiftTrap trap = new RiftTrap(user.world, (EntityPlayer) user.getOwner(), 0x87A363);
        trap.setPosition(user.posX, user.posY, user.posZ);
        trap.setRange(3f, 6f);
        trap.setCanExplode(3f, false);
        trap.setEffectToApply(MobEffects.POISON, 600, 2);
        user.world.spawnEntity(trap);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
