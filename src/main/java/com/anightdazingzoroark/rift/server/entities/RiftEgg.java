package com.anightdazingzoroark.rift.server.entities;

import com.anightdazingzoroark.rift.RiftInitialize;
import com.anightdazingzoroark.rift.client.ui.EggInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.Random;

public class RiftEgg extends TamableAnimal implements GeoAnimatable {
    private static final EntityDataAccessor<Integer> EGGTYPE = SynchedEntityData.defineId(RiftEgg.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HATCHTIME = SynchedEntityData.defineId(RiftEgg.class, EntityDataSerializers.INT);
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10D);
    }

    protected RiftEgg(EntityType<? extends TamableAnimal> p_21803_, Level p_21804_) {
        super(p_21803_, p_21804_);
    }

    public <T extends RiftCreature> T hatchTo(EntityType<T> entity) {
        if (this.isRemoved()) {
            return (T)null;
        }
        else {
            T t = entity.create(this.level);
            if (t == null) {
                return (T)null;
            }
            else {
                t.copyPosition(this);
                t.setBaby(true);
                t.setVariant(new Random().nextInt(4));
                if (this.hasCustomName()) {
                    t.setCustomName(this.getCustomName());
                    t.setCustomNameVisible(this.isCustomNameVisible());
                }

                this.level.addFreshEntity(t);

                this.discard();
                return t;
            }
        }
    }

    public boolean hurt(DamageSource damageSource, float damage) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        else {
            this.discard();
            return true;
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
//        System.out.println(this.getHatchTime()/20);
        Minecraft.getInstance().setScreen(new EggInfo(this));
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    public int getEggType() {
        return this.entityData.get(EGGTYPE);
    }

    public void setEggType(int type) {
        this.entityData.set(EGGTYPE, type);
    }

    public int getHatchTime() {
        return this.entityData.get(HATCHTIME);
    }

    public void setHatchTime(int time) {
        this.entityData.set(HATCHTIME, time);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EGGTYPE, 0);
        this.entityData.define(HATCHTIME, 0);
    }

    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("EggType", this.getEggType());
    }

    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setEggType(compoundTag.getInt("EggType"));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object o) {
        return 0;
    }
}
