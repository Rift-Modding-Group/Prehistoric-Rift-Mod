package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.PathNavigateRiftClimber;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import anightdazingzoroark.prift.server.entity.interfaces.ILeapAttackingMob;
import anightdazingzoroark.prift.server.entity.interfaces.IPackHunter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.*;

public class Utahraptor extends RiftCreature implements ILeapAttackingMob, IPackHunter, IHerder {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/utahraptor"));
    private static final DataParameter<Boolean> LEAPING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> PACK_BUFFING = EntityDataManager.createKey(Utahraptor.class, DataSerializers.BOOLEAN);
    private int packBuffCooldown;
    private float leapPower;
    private boolean startLeapingToTarget;
    private Entity contLeapTarget;
    private RiftCreaturePart neckPart;
    private RiftCreaturePart hipPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;
    protected int herdSize = 1;
    protected RiftCreature herdLeader;

    public Utahraptor(World worldIn) {
        super(worldIn, RiftCreatureType.UTAHRAPTOR);
        this.setSize(1.25f, 1.5f);
        this.experienceValue = 10;
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.speed = 0.35D;
        this.isRideable = true;
        this.packBuffCooldown = 0;
        this.maxRightClickCooldown = 1800f;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);

        this.headPart = new RiftCreaturePart(this, 2f, 0, 1.7f, 1f, 0.6f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0.8f, 0, 0.9f, 1f, 0.8f, 1f);
        this.neckPart = new RiftCreaturePart(this, 1.5f, 0, 1.2f, 0.7f, 0.7f, 1.5f);
        this.hipPart = new RiftCreaturePart(this, 0, 0, 0.7f, 1f, 1f, 1f);
        this.tail0Part = new RiftCreaturePart(this, -0.9f, 0, 1f, 0.7f, 0.6f, 0.5f);
        this.tail1Part = new RiftCreaturePart(this, -1.5f, 0, 0.95f, 0.6f, 0.6f, 0.5f);
        this.tail2Part = new RiftCreaturePart(this, -2.1f, 0, 0.9f, 0.6f, 0.6f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.neckPart,
            this.hipPart,
            this.tail0Part,
            this.tail1Part,
            this.tail2Part
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEAPING, false);
        this.dataManager.register(PACK_BUFFING, false);
        this.setCanPickUpLoot(true);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets(this, true, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftPackBuff(this, 1.68f, 0f, 90f));
        this.tasks.addTask(4, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(5, new RiftCreatureUseMoveUnmounted(this));
        this.tasks.addTask(7, new RiftFollowOwner(this, 1.0D, 8.0F, 4.0F));
        this.tasks.addTask(9, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(10, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(11, new RiftHerdMemberFollow(this));
        this.tasks.addTask(12, new RiftWander(this, 1.0D));
        this.tasks.addTask(13, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanLeap();
        this.manageCanPackBuff();
        if (!this.world.isRemote) {
            this.setClimbing(this.collidedHorizontally);

            //manage leaping
            if (this.onGround() && this.isLeaping()) {
                this.setLeaping(false);
                this.setControlledLeapTarget(null);
            }
            if (!this.onGround() && this.isLeaping() && this.isBeingRidden()) {
                if (this.contLeapTarget != null) {
                    AxisAlignedBB leapHithbox = this.getEntityBoundingBox().grow(0.75D);
                    List<EntityLivingBase> leapedEntities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, leapHithbox, null);
                    if (leapedEntities.contains(this.contLeapTarget)) {
                        this.attackEntityAsMob(this.contLeapTarget);
                        this.setControlledLeapTarget(null);
                    }
                }
            }
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.isSitting() && !this.isBeingRidden()) ? -0.45f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sitOffset, this.neckPart.posZ);
        if (this.hipPart != null) this.hipPart.setPositionAndUpdate(this.hipPart.posX, this.hipPart.posY + sitOffset, this.hipPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
    }

    private void manageCanLeap() {
        if (this.leapCooldown > 0) this.leapCooldown--;
    }

    private void manageCanPackBuff() {
        if (this.packBuffCooldown > 0) this.packBuffCooldown--;
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);
    }

    public void fall(float distance, float damageMultiplier) {}

    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateRiftClimber(this, worldIn);
    }

    public boolean isLeaping() {
        return this.dataManager.get(LEAPING);
    }

    public void setLeaping(boolean value) {
        this.dataManager.set(LEAPING, value);
        this.setActing(value);
    }

    public float getLeapPower() {
        return this.leapPower;
    }

    public void setLeapPower(float value) {
        this.leapPower = value;
    }

    public float leapWidth() {
        return 16f;
    }

    public Entity getControlledLeapTarget() {
        return this.contLeapTarget;
    }

    public void setControlledLeapTarget(Entity value) {
        this.contLeapTarget = value;
    }

    public boolean startLeapingToTarget() {
        return this.startLeapingToTarget;
    }

    public void setStartLeapToTarget(boolean value) {
        this.startLeapingToTarget = value;
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
    public boolean canBeSaddled() {
        return true;
    }

    @Override
    public int slotCount() {
        return 18;
    }

    public void setPackBuffing(boolean value) {
        this.dataManager.set(PACK_BUFFING, Boolean.valueOf(value));
        this.setActing(value);
    }

    public boolean isPackBuffing() {
        return this.dataManager.get(PACK_BUFFING);
    }

    public void setPackBuffCooldown(int value) {
        this.packBuffCooldown = value;
    }

    public int getPackBuffCooldown() {
        return this.packBuffCooldown;
    }

    public List<PotionEffect> packBuffEffect() {
        List<PotionEffect> packBuffEffects = new ArrayList<>();
        packBuffEffects.add(new PotionEffect(MobEffects.SPEED, 90 * 20, 2));
        packBuffEffects.add(new PotionEffect(MobEffects.STRENGTH, 90 * 20, 2));
        return packBuffEffects;
    }

    //move related stuff starts here
    @Override
    public List<CreatureMove> learnableMoves() {
        return Arrays.asList(CreatureMove.SCRATCH, CreatureMove.LEAP, CreatureMove.PACK_CALL);
    }

    @Override
    public List<CreatureMove> initialMoves() {
        return Arrays.asList(CreatureMove.SCRATCH, CreatureMove.LEAP, CreatureMove.PACK_CALL);
    }
    //move related stuff ends here

    public float attackWidth() {
        return 2f;
    }

    @Override
    public Map<CreatureMove.MoveType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveType.CLAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(1.5D)
                .defineRecoverFromUseLength(1D)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveType.STATUS, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(30D)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveType.CHARGE, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(6D)
                .defineChargeUpToUseLength(1D)
                .defineRecoverFromUseLength(1D)
                .finalizePoints());
        return moveMap;
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (0.05) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (0.05) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY - 0.75, zOffset);
    }

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {}

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
    public float[] ageScaleParams() {
        return new float[]{0.3f, 1f};
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.UTAHRAPTOR_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.UTAHRAPTOR_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.UTAHRAPTOR_DEATH;
    }

    public SoundEvent getCallSound() {
        return RiftSounds.UTAHRAPTOR_CALL;
    }
}
