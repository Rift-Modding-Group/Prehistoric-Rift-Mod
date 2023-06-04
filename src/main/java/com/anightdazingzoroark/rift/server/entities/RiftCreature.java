package com.anightdazingzoroark.rift.server.entities;

import com.anightdazingzoroark.rift.server.entities.creatures.TyrannosaurusEntity;
import com.anightdazingzoroark.rift.server.items.RiftItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Predicate;

public class RiftCreature extends TamableAnimal {
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(RiftCreature.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(RiftCreature.class, EntityDataSerializers.BOOLEAN);

    protected RiftCreature(EntityType<? extends TamableAnimal> type, Level world) {
        super(type, world);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        this.setVariant(new Random().nextInt(4));
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    public Predicate<ItemEntity> getFavoriteFoodItems() {
        return Entity::isAlive;
    }
    public Predicate<ItemEntity> getFavoriteTreats() {
        return Entity::isAlive;
    }

    public int getVariant() {
        return Mth.clamp(this.entityData.get(VARIANT), 0, 3);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean bool) {
        this.entityData.set(ATTACKING, bool);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, 0);
        this.entityData.define(ATTACKING, false);
    }

    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Variant", this.getVariant());
    }

    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setVariant(compoundTag.getInt("Variant"));
    }
    public boolean canPickUpLoot() {
        return true;
    }
    protected void pickUpItem(ItemEntity item) {
        ItemStack itemstack = item.getItem();
        ItemStack itemstack1 = this.equipItemIfPossible(itemstack.copy());
        if (!itemstack1.isEmpty() && (getFavoriteFoodItems().test(item) || getFavoriteTreats().test(item))) {
            this.onItemPickup(item);
            this.take(item, itemstack1.getCount());
            itemstack.shrink(itemstack1.getCount());
            this.getMainHandItem().setCount(0);
            if (itemstack.isEmpty()) {
                item.discard();
            }
        }
    }
    
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob ageableEntity) {
        return null;
    }
}
