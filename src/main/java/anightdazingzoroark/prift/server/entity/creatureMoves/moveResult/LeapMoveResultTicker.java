package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelperBase;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LeapMoveResultTicker extends AbstractMoveResultTicker {
    @Nullable
    private final EntityLivingBase target;
    private final boolean leapPrepared;
    private boolean hasLeftGround;
    private boolean hasDamagedTarget;

    public LeapMoveResultTicker(@NotNull RiftCreature creature, @NotNull MoveRuleBuilder moveRuleBuilder) {
        super(creature, moveRuleBuilder);
        EntityLivingBase attackTarget = creature.getAttackTarget();
        this.target = attackTarget != null && attackTarget.isEntityAlive() ? attackTarget : null;
        this.leapPrepared = this.prepareLeap();
        if (this.leapPrepared && creature.atFrustrationThreshold()) {
            creature.removeLeapToAttackCooldown();
            creature.resetFrustration();
        }
        else if (!this.leapPrepared && this.target != null) {
            // A target can fit beside a ledge where the creature cannot land.
            // Let ordinary move pathing run before retrying the rejected leap.
            creature.resetLeapToAttackCooldown();
            creature.getCreaturePathNavigate().tryMoveToEntityLiving(this.target, 1D);
        }
        if (this.leapPrepared) creature.getCreaturePathNavigate().clearPath();
    }

    @Override
    public boolean canContinueTicking() {
        return this.leapPrepared && this.creature.getCreatureMoveHelper().isLeaping();
    }

    @Override
    public void onUpdate() {
        if (!this.leapPrepared) return;

        if (!this.creature.onGround) this.hasLeftGround = true;
        if (this.creature.getCreatureType().getLeapDealsContactDamage()) this.tryDamageTargetOnContact();
    }

    @Override
    public void onEndTicker() {
        if (this.leapPrepared && this.hasLeftGround && this.creature.onGround) {
            if (this.creature.getCreatureType().getLeapDealsContactDamage()) this.tryDamageTargetOnContact();
        }
        this.creature.getCreaturePathNavigate().clearPath();
    }

    @Override
    public boolean isOverridableWhileUsed() {
        return false;
    }

    private boolean prepareLeap() {
        if (this.target == null) return false;
        RiftCreatureMoveHelperBase moveHelper = this.creature.getCreatureMoveHelper();
        if (this.creature.getCreatureType().getLeapDealsContactDamage()) {
            return moveHelper.setLeapTo(
                    this.target.posX,
                    this.target.posY,
                    this.target.posZ
            );
        }

        if (moveHelper.setLeapTo(this.target.posX, this.target.posY, this.target.posZ)) return true;

        if (this.creature.isUnableToPathToTarget()) return false;

        Vec3d gapLanding = moveHelper.getLeapHelper().findGapLeapLandingToward(
                this.target.posX,
                this.target.posY,
                this.target.posZ
        );
        return gapLanding != null && moveHelper.setLeapTo(gapLanding.x, gapLanding.y, gapLanding.z);
    }

    private void tryDamageTargetOnContact() {
        if (!this.hasLeftGround || this.hasDamagedTarget || this.target == null || !this.target.isEntityAlive()) return;
        if (!this.creature.getCreatureMoveHelper().getLeapHelper().touchesLeapTarget(
                this.creature.getEntityBoundingBox(), this.creature.getEntityBoundingBox(), this.target.getEntityBoundingBox())) return;

        this.creature.attackEntityFromLeap(this.target);
        this.hasDamagedTarget = true;
    }
}
