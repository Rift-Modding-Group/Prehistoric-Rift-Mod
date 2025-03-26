package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.config.DirewolfConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import anightdazingzoroark.prift.server.entity.interfaces.IImpregnable;
import anightdazingzoroark.prift.server.entity.interfaces.IPackHunter;
import anightdazingzoroark.prift.server.enums.MobSize;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSpawnChestDetectParticle;
import anightdazingzoroark.prift.server.message.RiftSpawnDetectParticle;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
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
import java.util.*;

public class Direwolf extends RiftCreature implements IImpregnable, IHerder {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/direwolf"));
    public static final DataParameter<Boolean> PREGNANT = EntityDataManager.createKey(Direwolf.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> PREGNANCY_TIMER = EntityDataManager.createKey(Direwolf.class, DataSerializers.VARINT);
    private int sniffCooldown;
    private RiftCreaturePart hipsPart;
    protected int herdSize = 1;
    protected RiftCreature herdLeader;

    public Direwolf(World worldIn) {
        super(worldIn, RiftCreatureType.DIREWOLF);
        this.setSize(1f, 1.55f);
        this.experienceValue = 10;
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.speed = 0.25D;
        this.isRideable = true;
        this.maxRightClickCooldown = 1800f;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.sniffCooldown = 0;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);

        this.headPart = new RiftCreaturePart(this, 1f, 0, 0.8f, 0.7f, 0.7f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.6f, 1f, 0.7f, 1f);
        this.hipsPart = new RiftCreaturePart(this, -0.9f, 0, 0.6f, 0.9f, 0.7f, 1f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.hipsPart
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PREGNANT, false);
        this.dataManager.register(PREGNANCY_TIMER, 0);
        this.setCanPickUpLoot(true);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets(this, true, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, false));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this, true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));

        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(4, new RiftCreatureUseMoveUnmounted(this));
        this.tasks.addTask(6, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(8, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(9, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(10, new RiftHerdMemberFollow(this));
        this.tasks.addTask(11, new RiftWander(this, 1.0D));
        this.tasks.addTask(12, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        //manage birthin related stuff
        if (!this.world.isRemote) this.createBaby(this);
    }

    @Override
    public void updateParts() {
        super.updateParts();

        if (this.isSitting() && !this.isBeingRidden() && this.hipsPart != null) {
            this.hipsPart.setPositionAndUpdate(this.hipsPart.posX, this.hipsPart.posY - 0.3f, this.hipsPart.posZ);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writePregnancyDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readPregnancyDataFromNBT(compound);
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
    public float[] ageScaleParams() {
        return new float[]{0.3f, 1.25f};
    }

    //move related stuff starts here
    @Override
    public List<CreatureMove> learnableMoves() {
        return Arrays.asList(CreatureMove.BITE, CreatureMove.SNARL, CreatureMove.POWER_BLOW, CreatureMove.PACK_CALL, CreatureMove.SNIFF);
    }

    @Override
    public List<CreatureMove> initialMoves() {
        return Arrays.asList(CreatureMove.BITE, CreatureMove.PACK_CALL, CreatureMove.SNIFF);
    }

    @Override
    public Map<CreatureMove.MoveType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveType.JAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveType.ROAR, new RiftCreatureMoveAnimator(this)
                .defineChargeUpToUseLength(5D)
                .defineUseDurationLength(30D)
                .defineRecoverFromUseLength(5D)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveType.STATUS, new RiftCreatureMoveAnimator(this)
                .defineChargeUpToUseLength(5D)
                .defineUseDurationLength(20D)
                .defineRecoverFromUseLength(5D)
                .finalizePoints());
        return moveMap;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 2.5f;
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-0.375f) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-0.375f) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY - 0.875, zOffset);
    }

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {
        /*
        if (control == 3 && this.sniffCooldown == 0 && this.headPart != null) {
            if (!this.headPart.isUnderwater()) {
                this.sniffCooldown = 100;
                //for all entities nearby (except those that are submerged)
                int mobSniffRange = RiftConfigHandler.getConfig(this.creatureType).general.mobSniffRange;
                AxisAlignedBB mobDetectAABB = new AxisAlignedBB(this.posX - mobSniffRange, this.posY - mobSniffRange, this.posZ - mobSniffRange, this.posX + mobSniffRange, this.posY + mobSniffRange, this.posZ + mobSniffRange);
                for (EntityLivingBase entityLivingBase : this.world.getEntitiesWithinAABB(EntityLivingBase.class, mobDetectAABB, null)) {
                    if (entityLivingBase != this && entityLivingBase != this.getOwner() && !RiftUtil.entityIsUnderwater(entityLivingBase) && RiftUtil.isAppropriateSize(entityLivingBase, MobSize.safeValueOf(RiftConfigHandler.getConfig(this.creatureType).general.maximumMobSniffSize))) {
                        RiftMessages.WRAPPER.sendToAll(new RiftSpawnDetectParticle((EntityPlayer)this.getControllingPassenger(), (int)entityLivingBase.posX, (int)entityLivingBase.posY, (int)entityLivingBase.posZ));
                    }
                }
                //for chests
                int blockSniffRange = RiftConfigHandler.getConfig(this.creatureType).general.blockSniffRange;
                for (int x = -blockSniffRange; x <= blockSniffRange; x++) {
                    for (int y = -blockSniffRange; y <= blockSniffRange; y++) {
                        for (int z = -blockSniffRange; z <= blockSniffRange; z++) {
                            BlockPos testPos = this.getPosition().add(x, y, z);
                            if (this.isSniffableBlock(this.world.getBlockState(testPos))) {
                                RiftMessages.WRAPPER.sendToAll(new RiftSpawnChestDetectParticle((EntityPlayer)this.getControllingPassenger(), testPos.getX(), testPos.getY(), testPos.getZ()));
                            }
                        }
                    }
                }
            }
        }
         */
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
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return RiftSounds.DIREWOLF_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.DIREWOLF_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.DIREWOLF_DEATH;
    }
}
