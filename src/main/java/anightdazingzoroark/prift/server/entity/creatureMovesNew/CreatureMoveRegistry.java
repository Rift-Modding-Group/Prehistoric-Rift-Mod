package anightdazingzoroark.prift.server.entity.creatureMovesNew;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.HashMap;
import java.util.List;

public class CreatureMoveRegistry {
    private static final HashMap<String, CreatureMoveBuilder> moveBuilderMap = new HashMap<>();

    public static boolean moveExists(String name) {
        return moveBuilderMap.containsKey(name);
    }

    public static CreatureMoveBuilder getCreatureMove(String name) {
        return moveBuilderMap.get(name);
    }

    public static void registerMove(String name, CreatureMoveBuilder builder) {
        if (moveBuilderMap.containsKey(name)) {
            RiftInitialize.logger.warn("Builder for move {} already exists!", name);
            return;
        }
        if (!builder.isValid()) {
            RiftInitialize.logger.warn("Builder for move type {} is invalid!", name);
            return;
        }
        builder.setName(name);
        moveBuilderMap.put(name, builder);
    }

    public static void createMoves() {
        registerMove(CreatureMoveNew.BITE, new CreatureMoveBuilder()
                .setBasePower(50)
                .setMakesContact()
                .setRequireFindTargetToUse()
                .setPhysical()
                .setCanUsePredicate(CreatureMoveNew.generalMeleePredicate)
                .setOnMoveHitEffect(CreatureMoveNew.generalAttackEntityEffect)
        );
        registerMove(CreatureMoveNew.STOMP, new CreatureMoveBuilder()
                .setBasePower(30)
                .setRequireFindTargetToUse()
                .setPhysical()
                .setUseCanStopMovement()
                .setCanUsePredicate(CreatureMoveNew.generalMeleePredicate)
                .setOnMoveHitEffect(creature -> {
                    AxisAlignedBB creatureAABB = creature.getEntityBoundingBox();
                    AxisAlignedBB stompRangeAABB = new AxisAlignedBB(
                            creatureAABB.minX - creature.getCreatureType().getPhysicalReach(),
                            creatureAABB.minY,
                            creatureAABB.minZ - creature.getCreatureType().getPhysicalReach(),
                            creatureAABB.maxX + creature.getCreatureType().getPhysicalReach(),
                            creatureAABB.minY + 1,
                            creatureAABB.maxZ + creature.getCreatureType().getPhysicalReach()
                    );

                    List<Entity> entitiesInStompRange = creature.world.getEntitiesWithinAABB(
                            Entity.class, stompRangeAABB
                    );
                    for (Entity entity : entitiesInStompRange) {
                        if (!(entity instanceof EntityLivingBase entityLivingBase)) continue;
                        if (creature.isRelatedToEntity(entityLivingBase)) continue;
                        if (entity.equals(creature)) continue;
                        creature.attackEntityAsMob(entityLivingBase);
                    }
                })
        );
        registerMove(CreatureMoveNew.THAGOMIZE, new CreatureMoveBuilder()
                .setBasePower(60)
                .setMakesContact()
                .setRequireFindTargetToUse()
                .setPhysical()
                .setUseCanStopMovement()
                .setCanUsePredicate(CreatureMoveNew.generalMeleePredicate)
                .setOnMoveHitEffect(CreatureMoveNew.generalAttackEntityEffect)
        );
    }
}
