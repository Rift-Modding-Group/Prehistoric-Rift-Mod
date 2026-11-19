package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.server.entity.RiftPetalStorm;
import net.minecraft.util.math.RayTraceResult;

public class PetalStormBuilder extends CreatureMoveBuilder {
    public PetalStormBuilder() {
        this.setBasePower(120)
                .setElement(Element.GRASS)
                .setMoveType(MoveType.SPECIAL)
                .setStaminaCost(0.3f)
                .setRequireFindTargetToUse()
                .setUseLookBasedFacing()
                .setCanUse(creature -> {
                    if (creature.getControllingPassenger() == null || creature.getRiddenAimPosition() == null) return true;
                    RayTraceResult hit = creature.getCrosshairTargets();
                    return hit != null && hit.typeOfHit != RayTraceResult.Type.MISS && RootSpikeBuilder.isWithinRange(creature, hit.hitVec);
                })
                .setCooldown(600)
                .setOnMoveHitEffect(RiftPetalStorm::spawn)
                .setRiddenAimingType(RiddenAimingType.TARGETED_MELEE)
                .setRiddenHorizontalRange(8D)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(RiddenCrosshairType.POINT);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("petal_storm", MoveCategory.RANGED);
            this.setPriorityPredicate(RootSpikeBuilder::canTarget);
        }
    }
}
