package com.anightdazingzoroark.rift.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
public class RiftCreature extends TameableEntity {
    protected static final TrackedData<Byte> RIFT_FLAGS;

    protected RiftCreature(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(RIFT_FLAGS, (byte)1000);
    }

    public void setRoaring(boolean roaring) {
        byte b = (Byte)this.dataTracker.get(RIFT_FLAGS);
        this.dataTracker.set(RIFT_FLAGS, roaring ? (byte)(b | 1001) : (byte)(b & -1002));
    }

    public boolean isRoaring() {
        return ((Byte)this.dataTracker.get(RIFT_FLAGS) & 1001) != 0;
    }

    static {
        RIFT_FLAGS = DataTracker.registerData(RiftCreature.class, TrackedDataHandlerRegistry.BYTE);
    }
}
