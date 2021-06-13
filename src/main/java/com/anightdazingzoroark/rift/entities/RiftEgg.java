package com.anightdazingzoroark.rift.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class RiftEgg extends PathAwareEntity implements IAnimatable {
    private static final TrackedData<Integer> EGG_TYPE = DataTracker.registerData(RiftEgg.class, TrackedDataHandlerRegistry.INTEGER);
    private final AnimationFactory factory = new AnimationFactory(this);

    protected RiftEgg(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 0);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    protected void pushAway(Entity entity) {}

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Type", this.getEggType());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setEggType(nbt.getInt("Type"));
    }

    public int getEggType() {
        return MathHelper.clamp((Integer)this.dataTracker.get(EGG_TYPE), 0, 7);
    }

    public void setEggType(int eggType) {
        this.dataTracker.set(EGG_TYPE, eggType);
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(EGG_TYPE, 0);
    }

    @Override
    public void registerControllers(AnimationData animationData) {

    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    public <T extends RiftCreature> T hatchTo(EntityType<T> entityType) {
        if (this.isRemoved()) {
            return null;
        }
        else {
            T riftCreature = (T) entityType.create(this.world);
            riftCreature.copyPositionAndRotation(this);
            riftCreature.setBaby(true);
            riftCreature.setVariant(random.nextInt(4));

            this.world.spawnEntity(riftCreature);

            this.discard();
            return riftCreature;
        }
    }
}
