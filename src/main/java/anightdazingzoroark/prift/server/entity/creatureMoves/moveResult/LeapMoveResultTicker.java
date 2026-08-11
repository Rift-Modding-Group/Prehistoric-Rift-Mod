package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelperBase;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveSelectorBuilder.LeapMoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.util.MathUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LeapMoveResultTicker extends AbstractMoveResultTicker {
    @NotNull
    private final LeapMoveRuleBuilder leapMoveRuleBuilder;
    @Nullable
    private final EntityLivingBase target;
    private final boolean leapPrepared;
    private boolean hasLeftGround;
    private boolean hasDamagedTarget;

    public LeapMoveResultTicker(@NotNull RiftCreature creature, @NotNull MoveRuleBuilder moveRuleBuilder) {
        super(creature, moveRuleBuilder);
        if (!(moveRuleBuilder instanceof LeapMoveRuleBuilder leapMoveRuleBuilder)) {
            throw new IllegalArgumentException("Leap move results require a leap move rule builder!");
        }
        this.leapMoveRuleBuilder = leapMoveRuleBuilder;

        EntityLivingBase attackTarget = creature.getAttackTarget();
        this.target = attackTarget != null && attackTarget.isEntityAlive() ? attackTarget : null;
        this.leapPrepared = this.prepareLeap();
        if (this.leapPrepared && creature.atFrustrationThreshold()) {
            creature.leapToAttackCooldown = 0;
            creature.resetFrustration();
        }
        else if (!this.leapPrepared
                && this.target != null
                && creature.getCreaturePathNavigate().tryMoveToEntityLivingUsingWater(this.target, 1D)) {
            creature.leapToAttackCooldown = MathUtil.randomInRange(creature.world.rand, 5, 10) * 20;
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
        if (this.leapMoveRuleBuilder.requiresTargetContact()) this.tryDamageTargetOnContact();
    }

    @Override
    public void onEndTicker() {
        if (this.leapPrepared && this.hasLeftGround && this.creature.onGround) {
            if (this.leapMoveRuleBuilder.requiresTargetContact()) this.tryDamageTargetOnContact();
        }
        if (this.leapPrepared) {
            this.creature.leapToAttackCooldown = MathUtil.randomInRange(this.creature.world.rand, 5, 10) * 20;
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
        if (this.leapMoveRuleBuilder.requiresTargetContact()) {
            return moveHelper.setLeapTo(
                    this.target.posX,
                    this.target.posY,
                    this.target.posZ
            );
        }

        if (moveHelper.setLeapTo(this.target.posX, this.target.posY, this.target.posZ)) return true;

        Vec3d gapLanding = moveHelper.getLeapHelper().findGapLeapLandingToward(
                this.target.posX,
                this.target.posY,
                this.target.posZ
        );
        return gapLanding != null && moveHelper.setLeapTo(gapLanding.x, gapLanding.y, gapLanding.z);
    }

    private void tryDamageTargetOnContact() {
        if (!this.hasLeftGround || this.hasDamagedTarget || this.target == null || !this.target.isEntityAlive()) return;
        if (!this.creature.getEntityBoundingBox().grow(1E-5D).intersects(this.target.getEntityBoundingBox())) return;

        this.creature.attackEntityFromLeap(this.target);
        this.hasDamagedTarget = true;
    }
}
