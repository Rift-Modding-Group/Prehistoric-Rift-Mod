package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.MoveRuleBuilder;
import anightdazingzoroark.prift.util.MathUtil;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

public class SprintMoveResultTicker extends AbstractMoveResultTicker {
    public SprintMoveResultTicker(@NotNull RiftCreature creature, @NotNull MoveRuleBuilder moveRuleBuilder) {
        super(creature, moveRuleBuilder);
        if (creature.atFrustrationThreshold()) {
            creature.sprintToAttackCooldown = 0;
            creature.resetFrustration();
        }
        creature.setSprinting(true);
    }

    @Override
    public boolean canContinueTicking() {
        EntityLivingBase target = this.creature.getAttackTarget();
        return this.creature.isSprinting() && target != null && target.isEntityAlive();
    }

    @Override
    public void onUpdate() {
        EntityLivingBase target = this.creature.getAttackTarget();
        if (target != null && target.isEntityAlive()) this.creature.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1D);
    }

    @Override
    public void onEndTicker() {
        this.creature.sprintToAttackCooldown = MathUtil.randomInRange(this.creature.world.rand, 5, 10) * 20;
        this.creature.setSprinting(false);
        this.creature.getNavigator().clearPath();

        //preserve last look direction after target is gone
        EntityLivingBase target = this.creature.getAttackTarget();
        if (target == null || !target.isEntityAlive()) this.preserveLastLookDirection();
    }
}
