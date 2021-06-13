package com.anightdazingzoroark.rift.entities;

import com.anightdazingzoroark.rift.entities.Creatures.TyrannosaurusEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
public class RiftCreature extends TameableEntity {
    protected static final TrackedData<Byte> RIFT_FLAGS = DataTracker.registerData(RiftCreature.class, TrackedDataHandlerRegistry.BYTE);;
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(RiftCreature.class, TrackedDataHandlerRegistry.INTEGER);

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
        this.dataTracker.startTracking(VARIANT, 0);
    }

    //for roaring stuff in case the creature can roar
    public void setRoaring(boolean roaring) {
        byte b = (Byte)this.dataTracker.get(RIFT_FLAGS);
        this.dataTracker.set(RIFT_FLAGS, roaring ? (byte)(b | 1001) : (byte)(b & -1002));
    }

    public boolean isRoaring() {
        return ((Byte)this.dataTracker.get(RIFT_FLAGS) & 1001) != 0;
    }

    //for tyranno hunting system (until more big predators get added)
    public void setHunting(boolean hunting) {
        byte b = (Byte)this.dataTracker.get(RIFT_FLAGS);
        this.dataTracker.set(RIFT_FLAGS, hunting ? (byte)(b | 1002) : (byte)(b & -1003));
    }

    public boolean isHunting() {
        return ((Byte)this.dataTracker.get(RIFT_FLAGS) & 1002) != 0;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", this.getVariant());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setVariant(nbt.getInt("Variant"));
    }

    public int getVariant() {
        return MathHelper.clamp((Integer)this.dataTracker.get(VARIANT), 0, 3);
    }

    public void setVariant(int variant) {
        this.dataTracker.set(VARIANT, variant);
    }
}
