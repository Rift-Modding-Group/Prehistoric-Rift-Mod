package anightdazingzoroark.prift.server.entity.creaturenew;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.dataSerializers.RiftDataSerializers;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.aiNew.RiftLookAroundNew;
import anightdazingzoroark.prift.server.entity.aiNew.RiftUnmountedUseMoveNew;
import anightdazingzoroark.prift.server.entity.aiNew.RiftWanderNew;
import anightdazingzoroark.prift.server.entity.aiNew.pathfinding.RiftCreatureMoveHelperNew;
import anightdazingzoroark.prift.server.entity.creatureMovesNew.*;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.info.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.hitbox.EntityHitbox;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;

public abstract class RiftCreatureNew extends EntityTameable implements IAnimatable<AnimationDataEntity>, /* IMultiHitboxUser, IDynamicRideUser,*/ IRiftCreature {
    private final RiftCreatureBuilder creatureType;
    private final RiftInventoryHandler creatureInventory;
    private final AnimationDataEntity factory = new AnimationDataEntity(this);

    public static final IAttribute ELEMENTAL_DAMAGE_ATTRIBUTE = new RangedAttribute(null, "rift.elementalDamage", 2.0, 0.0, 2048.0);
    public static final IAttribute STAMINA_ATTRIBUTE = new RangedAttribute(null, "rift.stamina", 2.0, 0.0, 2048.0);

    private static final DataParameter<Integer> LEVEL = EntityDataManager.createKey(RiftCreatureNew.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> NATURE = EntityDataManager.createKey(RiftCreatureNew.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.createKey(RiftCreatureNew.class, DataSerializers.VARINT);
    private static final DataParameter<Float> STAMINA_CURRENT = EntityDataManager.createKey(RiftCreatureNew.class, DataSerializers.FLOAT);
    private static final DataParameter<CreatureMoveStorage> CREATURE_MOVES = EntityDataManager.createKey(RiftCreatureNew.class, RiftDataSerializers.CREATURE_MOVE_STORAGE);
    private static final DataParameter<CreatureStatsStorage> CREATURE_STATS = EntityDataManager.createKey(RiftCreatureNew.class, RiftDataSerializers.CREATURE_STATS_STORAGE);
    private static final DataParameter<String> CREATURE_PHASE = EntityDataManager.createKey(RiftCreatureNew.class, DataSerializers.STRING);
    private static final DataParameter<String> CURRENTLY_USED_MOVE = EntityDataManager.createKey(RiftCreatureNew.class, DataSerializers.STRING);

    //manage sprint and sprint to attack
    public int sprintToAttackCooldown; //only matters when the creature is unmounted

    public RiftCreatureNew(World worldIn, String creatureName) {
        super(worldIn);
        this.creatureType = RiftCreatureRegistry.getCreatureBuilder(creatureName);
        this.moveHelper = new RiftCreatureMoveHelperNew(this);
        this.setSize(this.creatureType.getMainHitboxSize()[0], this.creatureType.getMainHitboxSize()[1]);
        this.creatureInventory = new RiftInventoryHandler(this.creatureType.getInventorySize());
        if (!this.creatureType.getCanBeKnockedBack()) this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
        if (this.creatureType.getCanSprintToAttack()) {
            this.sprintToAttackCooldown = RiftUtil.randomInRange(5, 10) * 20;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEVEL, 1);
        this.dataManager.register(NATURE, (byte) 0);
        this.dataManager.register(AGE_TICKS, 0);
        this.dataManager.register(STAMINA_CURRENT, 0f);
        this.dataManager.register(CREATURE_MOVES, new CreatureMoveStorage());
        this.dataManager.register(CREATURE_STATS, new CreatureStatsStorage());
        this.dataManager.register(CREATURE_PHASE, "");
        this.dataManager.register(CURRENTLY_USED_MOVE, "");
    }

    //this is gonna be mostly for registering the custom attributes
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        //vanilla ATTACK_DAMAGE is to be used for melee damage attribute
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getAttributeMap().registerAttribute(ELEMENTAL_DAMAGE_ATTRIBUTE);
        this.getAttributeMap().registerAttribute(STAMINA_ATTRIBUTE);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        //creature is to be an adult
        this.setAgeInTicks(this.creatureType.getDaysUntilAdult() * 24000);

        //set level based on distance from 0, 0
        double distFromCenter = Math.sqrt(this.posX * this.posX + this.posZ * this.posZ);
        double levelSlopeResult = RiftUtil.slopeResult(distFromCenter, false, 0, 1024, 1, 2);
        levelSlopeResult = RiftUtil.clamp(levelSlopeResult, 1, 10);
        levelSlopeResult = Math.round(levelSlopeResult);
        this.setLevel((int) levelSlopeResult);

        //initialize creature nature
        int randNatureIndex = this.rand.nextInt(RiftCreatureEnums.Nature.values().length);
        this.setNature(RiftCreatureEnums.Nature.values()[randNatureIndex]);

        //initialize creature stats
        CreatureStatsStorage creatureStatsStorage = this.getCreatureStats();
        creatureStatsStorage.initializeIndividualValues();
        creatureStatsStorage.parseStats(this.creatureType, this.getLevel(), this.getNature());
        creatureStatsStorage.applyStatsToCreature(this);
        this.setCreatureStats(creatureStatsStorage);

        //initialize move storage
        CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
        creatureMoveStorage.initLearnableMoves(this.creatureType.getLearnableMoves());
        creatureMoveStorage.initUsableMovesPerPhase(this.creatureType.getInitUsableMovesPerPhase());
        creatureMoveStorage.setCreaturePhase(this.getPhase());
        this.setCreatureMoves(creatureMoveStorage);

        //return value
        return super.onInitialSpawn(difficulty, livingdata);
    }

    //this is temporary for testing purposes, will be replaced w something more dynamic
    //after being developed further
    @Override
    protected void initEntityAI() {
        //temporary, will use the configs soon
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityCow.class, true));

        this.tasks.addTask(1, new RiftUnmountedUseMoveNew(this));
        this.tasks.addTask(2, new RiftWanderNew(this, 1D));
        this.tasks.addTask(3, new RiftLookAroundNew(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        //disable default growth system
        if (this.getGrowingAge() < 0) this.setGrowingAge(0);

        if (!this.world.isRemote) {
            this.setAgeInTicks(this.getAgeInTicks() + 1);

            //tick move cooldowns
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            creatureMoveStorage.tickCooldowns();
            this.setCreatureMoves(creatureMoveStorage);

            //tick sprinting related stuff
            if (this.creatureType.getCanSprintToAttack()) {
                if (this.sprintToAttackCooldown > 0) this.sprintToAttackCooldown--;
            }
        }
    }

    //this gets the scale of the model of the entity
    public float scale() {
        return RiftUtil.slopeResult(
                this.getAgeInTicks(), true,
                0, this.creatureType.getDaysUntilAdult() * 24000,
                this.creatureType.getScaleRangeForAge()[0], this.creatureType.getScaleRangeForAge()[1]
        );
    }

    //the vanilla attack entity method. is now used for damage calculations
    //use this when attacking an entity
    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (entityIn == null) return false;

        CreatureMoveBuilder creatureMoveBuilder = CreatureMoveRegistry.getCreatureMove(this.getCurrentMove());
        if (creatureMoveBuilder == null) return false;

        double damage = CreatureMoveNew.calculateDamage(this);
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float) damage);
        if (creatureMoveBuilder.getOnTargetHitEffect() != null && creatureMoveBuilder.getMakesContact()) {
            creatureMoveBuilder.getOnTargetHitEffect().accept(this, entityIn);
        }
        this.setLastAttackedEntity(entityIn);

        return flag;
    }

    //test if another entity is related to this creature
    //such as if its tamed to its owner
    public boolean isRelatedToEntity(Entity entity) {
        if (entity instanceof MultiPartEntityPart hitboxPart) {
            Entity hitboxParent = (Entity) hitboxPart.parent;
            return this.isRelatedToEntity(hitboxParent);
        }
        else if (entity instanceof EntityTameable entityTameable) {
            return entityTameable.isTamed() && entityTameable.getOwner() != null && entityTameable.getOwner().equals(this.getOwner());
        }
        else if (entity instanceof EntityPlayer entityPlayer) {
            return this.isTamed() && this.getOwner() != null && this.getOwner().equals(entityPlayer);
        }
        return false;
    }

    //-----creature phase management-----
    public String getPhase() {
        return this.dataManager.get(CREATURE_PHASE);
    }

    public void setPhase(String value) {
        if (value == null) return;
        this.dataManager.set(CREATURE_PHASE, value);
    }

    //-----move use management-----
    public String getCurrentMove() {
        return this.dataManager.get(CURRENTLY_USED_MOVE);
    }

    public void setCurrentMove(String name) {
        this.dataManager.set(CURRENTLY_USED_MOVE, name);
    }

    public void resetCurrentMove() {
        this.setCurrentMove("");
    }

    //-----IRiftCreature boilerplate stuff-----
    @Override
    public RiftCreatureBuilder getCreatureType() {
        return this.creatureType;
    }

    @Override
    public int getLevel() {
        return this.dataManager.get(LEVEL);
    }

    @Override
    public void setLevel(int value) {
        this.dataManager.set(LEVEL, value);
    }

    @Override
    public RiftCreatureEnums.Nature getNature() {
        byte natureOrdinal = this.dataManager.get(NATURE);
        if (natureOrdinal < 0 || natureOrdinal >= RiftCreatureEnums.Nature.values().length) return null;
        return RiftCreatureEnums.Nature.values()[natureOrdinal];
    }

    @Override
    public void setNature(RiftCreatureEnums.Nature value) {
        byte byteToSet = value != null ? (byte) value.ordinal() : (byte) -1;
        this.dataManager.set(NATURE, byteToSet);
    }

    @Override
    public int getAgeInTicks() {
        return this.dataManager.get(AGE_TICKS);
    }

    public int getAgeInDays() {
        return this.getAgeInTicks() / 20;
    }

    @Override
    public void setAgeInTicks(int value) {
        this.dataManager.set(AGE_TICKS, value);
    }

    @Override
    public float getStamina() {
        return this.dataManager.get(STAMINA_CURRENT);
    }

    @Override
    public void setStamina(float value) {
        this.dataManager.set(STAMINA_CURRENT, value);
    }

    @Override
    public float getMaxStamina() {
        return (float) this.getEntityAttribute(STAMINA_ATTRIBUTE).getAttributeValue();
    }

    @Override
    public RiftInventoryHandler getCreatureInventory() {
        return this.creatureInventory;
    }

    @Override
    public CreatureStatsStorage getCreatureStats() {
        return this.dataManager.get(CREATURE_STATS);
    }

    @Override
    public void setCreatureStats(CreatureStatsStorage value) {
        this.dataManager.set(CREATURE_STATS, value);
    }

    @Override
    public CreatureMoveStorage getCreatureMoves() {
        return this.dataManager.get(CREATURE_MOVES);
    }

    @Override
    public void setCreatureMoves(CreatureMoveStorage value) {
        this.dataManager.set(CREATURE_MOVES, value);
    }

    //-----nbt parsing related stuff-----
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writeCreatureNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readCreatureNBT(compound);
    }

    //-----hitbox related methods-----
    /*
    @Override
    public Entity getMultiHitboxUser() {
        return this;
    }

    @Override
    public void setParts(Entity[] entities) {

    }

    @Override
    public World getWorld() {
        return this.world;
    }
     */

    //-----animation related methods-----
    @Override
    public void registerControllers(AnimationDataEntity animationData) {
        animationData.addAnimationController(new AnimationController<>(
                this, "movement", 0,
                event -> {
                    if (animationData.isMoving()) {
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+this.creatureType.getName()+".walk", LoopType.LOOP));
                        return PlayState.CONTINUE;
                    }
                    event.getController().clearAnimationCache();
                    return PlayState.STOP;
                }
        ));
        animationData.addAnimationController(new AnimationController<>(
                this, "moveUse", 0,
                event -> {
                    if (!this.getCurrentMove().isEmpty()) {
                        String animName = this.getCreatureMoves().getAnimationNameForMove(this.getCurrentMove());
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+this.creatureType.getName()+"."+animName, LoopType.PLAY_ONCE));
                        return PlayState.CONTINUE;
                    }
                    event.getController().clearAnimationCache();
                    return PlayState.STOP;
                }
        ));
    }

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.factory;
    }

    @Override
    public HashMap<String, Runnable> animationMessageEffects() {
        HashMap<String, Runnable> toReturn = new HashMap<>();
        toReturn.put("moveHitEffect", () -> {
            if (this.getCurrentMove().isEmpty()) return;

            CreatureMoveBuilder creatureMoveBuilder = CreatureMoveRegistry.getCreatureMove(this.getCurrentMove());
            creatureMoveBuilder.getOnMoveHitEffect().accept(this);
        });
        toReturn.put("endMoveEffect", this::resetCurrentMove);
        return toReturn;
    }

    //-----dynamic ride position methods-----
    /*
    @Override
    public EntityLiving getDynamicRideUser() {
        return this;
    }

    @Override
    public DynamicRidePosList ridePosList() {
        return null;
    }

    @Override
    public void setRidePosition(DynamicRidePosList dynamicRidePosList) {

    }
     */

    //-----other useless events idk nor care about-----
    @Override
    public @Nullable EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
