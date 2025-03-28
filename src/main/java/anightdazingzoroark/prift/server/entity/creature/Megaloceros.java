package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.config.MegalocerosConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Megaloceros extends RiftCreature implements IImpregnable, IHarvestWhenWandering, IHerder {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/megaloceros"));
    public static final DataParameter<Boolean> PREGNANT = EntityDataManager.createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> PREGNANCY_TIMER = EntityDataManager.createKey(Megaloceros.class, DataSerializers.VARINT);
    public static final DataParameter<Boolean> HARVESTING = EntityDataManager.createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> CAN_HARVEST = EntityDataManager.createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    private RiftCreaturePart frontBodyPart;
    protected int herdSize = 1;
    protected RiftCreature herdLeader;

    public Megaloceros(World worldIn) {
        super(worldIn, RiftCreatureType.MEGALOCEROS);
        this.setSize(1f, 1.5f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 10;
        this.speed = 0.35D;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;

        this.headPart = new RiftCreaturePart(this, 1.4f, 0, 1.3f, 0.7f, 0.6f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.75f, 1f, 0.75f, 1f);
        this.frontBodyPart = new RiftCreaturePart(this, 0.8f, 0, 0.75f, 0.75f, 0.75f, 1f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.frontBodyPart
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PREGNANT, false);
        this.dataManager.register(PREGNANCY_TIMER, 0);
        this.dataManager.register(HARVESTING, false);
        this.dataManager.register(CAN_HARVEST, false);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, false));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));

        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));

        this.tasks.addTask(3, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(4, new RiftCreatureUseMoveUnmounted(this));
        this.tasks.addTask(6, new RiftHarvestOnWander(this, 0.52F, 0.36F));
        this.tasks.addTask(7, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(9, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(10, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(11, new RiftHerdMemberFollow(this));
        this.tasks.addTask(12, new RiftWander(this, 1.0D));
        this.tasks.addTask(13, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote) {
            //manage birthin related stuff
            this.createBaby(this);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.isSitting() && !this.isBeingRidden()) ? -0.55f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.frontBodyPart != null) this.frontBodyPart.setPositionAndUpdate(this.frontBodyPart.posX, this.frontBodyPart.posY + sitOffset, this.frontBodyPart.posZ);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (this.isTamed() && this.getOwner().equals(player) && !this.isBaby() && itemstack.getItem() == Items.BUCKET) {
            player.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);
            itemstack.shrink(1);

            if (itemstack.isEmpty()) player.setHeldItem(hand, new ItemStack(Items.MILK_BUCKET));
            else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.MILK_BUCKET))) {
                player.dropItem(new ItemStack(Items.MILK_BUCKET), false);
            }
            return true;
        }
        else return super.processInteract(player, hand);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writePregnancyDataToNBT(compound);
        this.writeHarvestWanderDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readPregnancyDataFromNBT(compound);
        this.readHarvestWanderDataFromNBT(compound);
    }

    @Override
    public boolean canDoHerding() {
        return !this.isTamed();
    }

    public RiftCreature getHerder() {
        return this;
    }

    public RiftCreature getHerdLeader() {
        return this.herdLeader;
    }

    public void setHerdLeader(RiftCreature creature) {
        this.herdLeader = creature;
    }

    public int getHerdSize() {
        return this.herdSize;
    }

    public void setHerdSize(int value) {
        this.herdSize = value;
    }

    public double followRange() {
        return 4D;
    }

    @Override
    public int slotCount() {
        return 18;
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 1.125f};
    }

    //move related stuff starts here
    @Override
    public List<CreatureMove> learnableMoves() {
        return Arrays.asList(CreatureMove.HEADBUTT, CreatureMove.CHARGE, CreatureMove.LEAP);
    }

    @Override
    public List<CreatureMove> initialMoves() {
        return Arrays.asList(CreatureMove.HEADBUTT, CreatureMove.CHARGE, CreatureMove.LEAP);
    }

    @Override
    public Map<CreatureMove.MoveType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveType.HEAD, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveType.LEAP, new RiftCreatureMoveAnimator(this)
                .defineChargeUpToUseLength(5D)
                .defineRecoverFromUseLength(1D)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveType.CHARGE, new RiftCreatureMoveAnimator(this)
                .defineStartMoveDelayLength(5D)
                .defineRecoverFromUseLength(5D)
                .finalizePoints());
        return moveMap;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 2.5f;
    }

    public float rangedWidth() {
        return 32f;
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-0.125f) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-0.125f) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY - 0.75, zOffset);
    }

    public void setPregnant(boolean value, int timer) {
        this.dataManager.set(PREGNANT, value);
        this.dataManager.set(PREGNANCY_TIMER, timer);
    }

    public boolean isPregnant() {
        return this.dataManager.get(PREGNANT);
    }

    public void setPregnancyTimer(int value) {
        this.dataManager.set(PREGNANCY_TIMER, value);
    }

    public int getPregnancyTimer() {
        return this.dataManager.get(PREGNANCY_TIMER);
    }

    @Override
    public boolean hasLeftClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasSpacebarChargeBar() {
        return false;
    }

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {}

    @Override
    public List<String> blocksToHarvest() {
        return RiftConfigHandler.getConfig(this.creatureType).general.harvestableBlocks;
    }

    public int harvestRange() {
        return 3;
    }

    public void setHarvesting(boolean value) {
        this.setAttacking(value);
    }

    public boolean isHarvesting() {
        return this.isAttacking();
    }

    public void setCanHarvest(boolean value) {
        this.dataManager.set(CAN_HARVEST, value);
    }

    public boolean canHarvest() {
        return this.dataManager.get(CAN_HARVEST);
    }

    @Override
    public AxisAlignedBB breakRange() {
        return new AxisAlignedBB(-1, -1, -1, 1, 1, 1);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return RiftSounds.MEGALOCEROS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.MEGALOCEROS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.MEGALOCEROS_DEATH;
    }
}
