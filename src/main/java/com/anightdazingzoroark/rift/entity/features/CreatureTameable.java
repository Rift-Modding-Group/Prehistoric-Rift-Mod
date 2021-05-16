package com.anightdazingzoroark.rift.entity.features;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.world.World;

public abstract class CreatureTameable extends TameableEntity {
    protected CreatureTameable(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }
}
