package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class AllyCallUtil {
    public static void callAllies(@NotNull ICreature user) {
        if (!(user instanceof RiftCreature caller) || caller.world.isRemote || caller.isTemporaryAlly()) return;
        if (!caller.isTamed() && (!caller.canDoHerding() || caller.isInHerd())) return;
        if (caller.isTamed() && caller.getOwnerId() == null) return;

        int maximumAllies = caller.isTamed() ? 3 : Math.min(3, caller.getCreatureType().getMaxHerdSize() - 1);
        for (int index = 0; index < maximumAllies; index++) {
            RiftCreature ally = RiftCreatureRegistry.createCreature(caller.world, caller.getCreatureType().getName());
            boolean foundPosition = false;
            for (int attempt = 0; attempt < 24; attempt++) {
                double angle = caller.getRNG().nextDouble() * Math.TAU;
                double radius = caller.width + ally.width + 1D + caller.getRNG().nextDouble() * 3D;
                BlockPos column = new BlockPos(caller.posX + Math.cos(angle) * radius, caller.posY, caller.posZ + Math.sin(angle) * radius);
                for (int heightOffset = 2; heightOffset >= -3; heightOffset--) {
                    BlockPos position = column.add(0, heightOffset, 0);
                    if (!caller.world.isBlockLoaded(position) || !caller.world.getWorldBorder().contains(position)) continue;
                    if (!caller.world.getBlockState(position.down()).isSideSolid(caller.world, position.down(), EnumFacing.UP)) continue;

                    ally.setLocationAndAngles(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D, caller.rotationYaw, 0f);
                    if (caller.world.containsAnyLiquid(ally.getEntityBoundingBox())
                            || !caller.world.getCollisionBoxes(ally, ally.getEntityBoundingBox()).isEmpty()
                            || !caller.world.checkNoEntityCollision(ally.getEntityBoundingBox(), ally)
                    ) {
                        continue;
                    }
                    foundPosition = true;
                    break;
                }
                if (foundPosition) break;
            }
            if (!foundPosition) continue;

            ally.onInitialSpawn(caller.world.getDifficultyForLocation(ally.getPosition()), null);
            ally.setLevel(Math.max(1, caller.getLevel() - caller.getRNG().nextInt(1, 3)));
            ally.getCreatureStats().applyStatsToCreature(ally);
            ally.setHealth(ally.getMaxHealth());
            ally.enablePersistence();

            if (caller.isTamed()) {
                ally.setTamed(true);
                ally.setOwnerId(caller.getOwnerId());
                ally.setTemporaryAllyLifetime(900);
                ally.setTameTargeting(RiftCreatureEnums.TameTargeting.ASSIST);
            }

            if (!caller.world.spawnEntity(ally)) continue;
            if (!caller.isTamed() && caller.getHerd() != null && ally.getHerd() != null) {
                caller.getHerd().mergeWith(ally.getHerd());
            }
            ally.setAttackTarget(caller.getAttackTarget());
        }
    }
}
