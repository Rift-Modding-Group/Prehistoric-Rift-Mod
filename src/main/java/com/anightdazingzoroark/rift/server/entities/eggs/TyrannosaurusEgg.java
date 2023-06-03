package com.anightdazingzoroark.rift.server.entities.eggs;

import com.anightdazingzoroark.rift.server.entities.RiftEgg;
import com.anightdazingzoroark.rift.server.entities.RiftEntityRegistry;
import com.anightdazingzoroark.rift.server.entities.creatures.TyrannosaurusEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class TyrannosaurusEgg extends RiftEgg {
    private static final EntityDataAccessor<Integer> HATCHTIME = SynchedEntityData.defineId(RiftEgg.class, EntityDataSerializers.INT);

    public TyrannosaurusEgg(EntityType<? extends TamableAnimal> p_21803_, Level p_21804_) {
        super(p_21803_, p_21804_);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        this.setEggType(0);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override
    public void tick() {
        super.tick();
        this.setHatchTime(this.getHatchTime() - 20);
        if (this.getHatchTime() <= 0) {
            TyrannosaurusEntity tyrannosaurus = this.hatchTo(RiftEntityRegistry.TYRANNOSAURUS.get());
            if (tyrannosaurus != null) {
                tyrannosaurus.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
                tyrannosaurus.setHealth(20.0F);
                tyrannosaurus.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6.0D);
                tyrannosaurus.goalSelector.addGoal(4, tyrannosaurus.attackAnimalsGoal);
            }
        }
    }

    public int getHatchTime() {
        return this.entityData.get(HATCHTIME);
    }

    public void setHatchTime(int time) {
        this.entityData.set(HATCHTIME, time);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HATCHTIME, 10 * 20);
    }

    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("HatchTime", this.getHatchTime());
    }
}
