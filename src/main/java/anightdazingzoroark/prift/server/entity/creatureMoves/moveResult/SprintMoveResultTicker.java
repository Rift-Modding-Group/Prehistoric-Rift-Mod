package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.MoveRuleBuilder;
import anightdazingzoroark.prift.util.MathUtil;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SprintMoveResultTicker extends AbstractMoveResultTicker {
    private boolean hasHitWhileSprinting;

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
        return target != null && target.isEntityAlive() && !this.hasHitWhileSprinting;
    }

    @Override
    public void onUpdate() {
        EntityLivingBase target = this.creature.getAttackTarget();
        if (target != null && target.isEntityAlive()) this.creature.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1D);

        //check for anything inside front AABBs
        List<AnimatedBoundingBox> frontZoneAnimatedBBs = this.creature.getAnimationData().getAnimatedBoundingBoxesByTag().get("frontZone");
        if (frontZoneAnimatedBBs != null) {
            //fill up list of hit entities
            List<EntityLivingBase> allHitEntities = new ArrayList<>();
            for (AnimatedBoundingBox frontZoneAnimatedBB : frontZoneAnimatedBBs) {
                AxisAlignedBB aabb = this.creature.getAnimationData().getWorldSpaceAABB(frontZoneAnimatedBB.getName());
                List<EntityLivingBase> hitEntities = this.creature.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)
                        .stream().filter(entity -> {
                            return entity != null && !allHitEntities.contains(entity) && !this.creature.equals(entity) && !this.creature.isRelatedToEntity(entity);
                        }).toList();
                allHitEntities.addAll(hitEntities);
            }

            //apply damage
            if (!allHitEntities.isEmpty()) {
                for (Entity hitEntity : allHitEntities) this.creature.attackEntityFromSprint(hitEntity);
                this.hasHitWhileSprinting = true;
            }
        }
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

    @Override
    public boolean isOverridableWhileUsed() {
        return false;
    }
}
