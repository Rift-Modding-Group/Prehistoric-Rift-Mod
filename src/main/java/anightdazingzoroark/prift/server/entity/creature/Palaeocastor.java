package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.*;

public class Palaeocastor extends RiftCreature implements IHarvestWhenWandering {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/palaeocastor"));
    public static final DataParameter<Boolean> HARVESTING = EntityDataManager.createKey(Palaeocastor.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> CAN_HARVEST = EntityDataManager.createKey(Palaeocastor.class, DataSerializers.BOOLEAN);

    public Palaeocastor(World worldIn) {
        super(worldIn, RiftCreatureType.PALAEOCASTOR);
        this.setSize(0.75f, 0.75f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.speed = 0.25D;
        this.experienceValue = 3;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HARVESTING, false);
        this.dataManager.register(CAN_HARVEST, false);
    }

    @Override
    public void updateParts() {
        super.updateParts();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writeHarvestWanderDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readHarvestWanderDataFromNBT(compound);
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.25f, 1f};
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(2, Arrays.asList(CreatureMove.BITE, CreatureMove.SCRATCH, CreatureMove.BURROW));
        possibleMoves.add(2, Arrays.asList(CreatureMove.BITE, CreatureMove.TACKLE, CreatureMove.BURROW));
        possibleMoves.add(2, Arrays.asList(CreatureMove.SCRATCH, CreatureMove.TACKLE, CreatureMove.BURROW));
        possibleMoves.add(1, Arrays.asList(CreatureMove.BITE, CreatureMove.SCRATCH, CreatureMove.MUDBALL));
        possibleMoves.add(1, Arrays.asList(CreatureMove.BITE, CreatureMove.TACKLE, CreatureMove.MUDBALL));
        possibleMoves.add(1, Arrays.asList(CreatureMove.SCRATCH, CreatureMove.TACKLE, CreatureMove.MUDBALL));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.JAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(1.25D)
                .defineChargeUpToUseLength(1.25D)
                .defineRecoverFromUseLength(2.5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_BITE_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.CLAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineChargeUpToUseLength(2D)
                .defineRecoverFromUseLength(3D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_SCRATCH_MOVE)
                .setNumberOfAnims(2)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.CHARGE, new RiftCreatureMoveAnimator(this)
                .defineStartMoveDelayLength(5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.PALAEOCASTOR_CHARGE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.BURROW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(7.5D)
                .defineUseDurationLength(5D)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.RANGED, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.PALAEOCASTOR_MUDBALL)
                .finalizePoints());
        return moveMap;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 2f;
    }

    @Override
    public List<String> blocksToHarvest() {
        return RiftConfigHandler.getConfig(this.creatureType).general.harvestableBlocks;
    }

    public void setHarvesting(boolean value) {
        this.dataManager.set(HARVESTING, value);
    }

    public boolean isHarvesting() {
        return this.dataManager.get(HARVESTING);
    }

    public void setCanHarvest(boolean value) {
        this.dataManager.set(CAN_HARVEST, value);
    }

    public boolean canHarvest() {
        return this.dataManager.get(CAN_HARVEST);
    }

    @Override
    public int slotCount() {
        return 9;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return RiftSounds.PALAEOCASTOR_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.PALAEOCASTOR_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.PALAEOCASTOR_DEATH;
    }

    public SoundEvent getWarnSound() {
        return RiftSounds.PALAEOCASTOR_WARN;
    }
}
