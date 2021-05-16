package com.anightdazingzoroark.rift.entity.features;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

public class CreatureUntameable extends PathAwareEntity {
    protected CreatureUntameable(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }
}
