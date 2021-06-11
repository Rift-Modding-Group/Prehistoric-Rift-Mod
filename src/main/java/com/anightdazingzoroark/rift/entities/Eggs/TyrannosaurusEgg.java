package com.anightdazingzoroark.rift.entities.Eggs;

import com.anightdazingzoroark.rift.entities.RiftCreature;
import com.anightdazingzoroark.rift.entities.RiftEgg;
import com.anightdazingzoroark.rift.registry.ModEntities;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TyrannosaurusEgg extends RiftEgg {
    private int hatchTick = 100;

    public TyrannosaurusEgg(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        this.setEggType(0);
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void mobTick() {
        this.hatchTick--;
        if (this.hatchTick <= 0) {
            this.hatchTo(ModEntities.TYRANNOSAURUS);
        }
    }
}
