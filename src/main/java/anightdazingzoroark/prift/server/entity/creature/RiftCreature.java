package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.entity.creature.builder.CreaturePhaseBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureStatsStorage;
import anightdazingzoroark.prift.server.dataSerializers.RiftDataSerializers;
import anightdazingzoroark.prift.server.entity.ai.RiftUnmountedUseMove;
import anightdazingzoroark.prift.server.entity.ai.RiftWander;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.RiftCreatureEnums;
import anightdazingzoroark.prift.util.MathUtil;
import anightdazingzoroark.prift.util.TriConsumer;
import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationDataEntity;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.hitbox.HitboxDefinitionList;
import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.AbstractPropertyValue;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RiftCreature extends EntityTameable implements IAnimatable<AnimationDataEntity>, IRiftCreature {
    @NotNull
    private final RiftCreatureBuilder creatureType;
    @NotNull
    private final RiftLibInventoryHandler creatureInventory;
    @NotNull
    private final AnimationDataEntity animData;

    public static final IAttribute ELEMENTAL_DAMAGE_ATTRIBUTE = new RangedAttribute(null, "rift.elementalDamage", 2.0, 0.0, 2048.0);
    public static final IAttribute STAMINA_ATTRIBUTE = new RangedAttribute(null, "rift.stamina", 2.0, 0.0, 2048.0);

    private static final DataParameter<Integer> LEVEL = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> NATURE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Float> STAMINA_CURRENT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.FLOAT);
    private static final DataParameter<CreatureMoveStorage> CREATURE_MOVES = EntityDataManager.createKey(RiftCreature.class, RiftDataSerializers.CREATURE_MOVE_STORAGE);
    private static final DataParameter<CreatureStatsStorage> CREATURE_STATS = EntityDataManager.createKey(RiftCreature.class, RiftDataSerializers.CREATURE_STATS_STORAGE);
    private static final DataParameter<String> CREATURE_PHASE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.STRING);

    //custom property values, which can be called and manipulated from a creature builder
    private final Map<String, AbstractPropertyValue<?>> propertyValueMap;

    //server side primitive params
    public int sprintToAttackCooldown; //manages a creature's ability to sprint based on whether or not it attacked before
    public int timeSinceAggression; //how much time ever since it encountered a target

    //ray specific params
    protected Map<String, RiftLibRayBuilder> rayMap;
    protected Map<String, TriConsumer<RiftCreature, BlockPos, RiftLibRay.RayHitResult>> rayHitEffectMap;

    public RiftCreature(World worldIn, String creatureName) {
        super(worldIn);
        this.creatureType = RiftCreatureRegistry.getCreatureBuilder(creatureName);
        this.animData = new AnimationDataEntity(this);
        this.moveHelper = new RiftCreatureMoveHelper(this);
        this.setSize(this.creatureType.getMainHitboxSize()[0], this.creatureType.getMainHitboxSize()[1]);
        this.creatureInventory = new RiftLibInventoryHandler(this.creatureType.getInventorySize());

        if (this.creatureType.getPropertyValueMap() != null) this.propertyValueMap = new HashMap<>(this.creatureType.getPropertyValueMap());
        else this.propertyValueMap = Map.of();

        if (!this.creatureType.getCanBeKnockedBack()) this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);

        if (this instanceof IRayCreator<?> && this.creatureType.getRayMap() != null && this.creatureType.getRayHitEffectMap() != null) {
            //todo: add exception if this creature is rayuser instance
            //but !(this.creatureType.getRayMap() != null && this.creatureType.getRayHitEffectMap() != null) is true
            //if (this.creatureType.getRayMap() == null)
            this.rayMap = this.creatureType.getRayMap();
            this.rayHitEffectMap = this.creatureType.getRayHitEffectMap();
        }

        /*
        if (this.creatureType.getCanSprintToAttack()) {
            this.sprintToAttackCooldown = RiftUtil.randomInRange(5, 10) * 20;
        }
         */
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
        double levelSlopeResult = MathUtil.slopeResult(distFromCenter, false, 0, 1024, 1, 2);
        levelSlopeResult = Math.clamp(levelSlopeResult, 1, 10);
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

        //initialize creature moves
        CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
        creatureMoveStorage.setCreatureUser(this.creatureType);
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

        this.tasks.addTask(1, new RiftUnmountedUseMove(this));
        this.tasks.addTask(2, new EntityAIWander(this, 1D, 15));
        this.tasks.addTask(3, new EntityAILookIdle(this) {
            @Override
            public void resetTask() {
                this.idleTime = 0;
            }
        });
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        //disable default growth system
        if (this.getGrowingAge() < 0) this.setGrowingAge(0);

        //server only operations
        if (!this.world.isRemote) {
            //set age
            this.setAgeInTicks(this.getAgeInTicks() + 1);

            //tick time since aggression
            if (this.getAttackTarget() != null) this.timeSinceAggression++;
            else this.timeSinceAggression = 0;

            //tick creature move storage
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            creatureMoveStorage.updateUsableMoves(this, this.getAttackTarget());
            creatureMoveStorage.tickCooldowns();
            this.setCreatureMoves(creatureMoveStorage);

            //tick sprinting related stuff
            if (this.sprintToAttackCooldown > 0) this.sprintToAttackCooldown--;
            if (this.isSprinting() && this.sprintToAttackCooldown <= 0) {
                AxisAlignedBB frontAABB = this.getFrontAABB();
                List<EntityLivingBase> hitEntities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, frontAABB)
                        .stream().filter(entity -> {
                             return entity != null && !this.equals(entity) && !this.isRelatedToEntity(entity);
                        }).toList();

                if (!hitEntities.isEmpty()) {
                    for (Entity hitEntity : hitEntities) this.attackEntityFromSprint(hitEntity);

                    this.sprintToAttackCooldown = MathUtil.randomInRange(this.world.rand, 5, 10) * 20;
                    this.setSprinting(false);
                }
            }

            //tick the creature on tick lambda
            if (this.creatureType.getUpdateEffect() != null) this.creatureType.getUpdateEffect().accept(this);
        }
    }

    //this creates an AABB that is ahead of is center by 1.5 blocks + its width
    public AxisAlignedBB getFrontAABB() {
        double frontWidth = this.width / 2D + 1.5D;
        double frontHeight = this.height;
        Vec3d look = this.getLookVec().normalize();
        double perpX = -look.z;
        double perpZ = look.x;
        double centerX = this.posX + look.x * this.width;
        double centerZ = this.posZ + look.z * this.width;

        double startX = centerX - perpX * frontWidth;
        double startZ = centerZ - perpZ * frontWidth;
        double endX = centerX + perpX * frontWidth;
        double endZ = centerZ + perpZ * frontWidth;

        return new AxisAlignedBB(
                Math.min(startX, endX),
                this.posY,
                Math.min(startZ, endZ),
                Math.max(startX, endX),
                this.posY + frontHeight,
                Math.max(startZ, endZ)
        );
    }

    //this gets the scale of the model of the entity
    public float scale() {
        return MathUtil.slopeResult(
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

        CreatureMoveBuilder creatureMoveBuilder = this.getCreatureMoves().getMoveBuilderCurrentMove();
        if (creatureMoveBuilder == null) return false;

        double damage = CreatureMoveHelper.calculateDamage(this);
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float) damage);
        if (creatureMoveBuilder.getOnTargetHitEffect() != null && creatureMoveBuilder.getMakesContact()) {
            creatureMoveBuilder.getOnTargetHitEffect().accept(this, entityIn);
        }
        this.setLastAttackedEntity(entityIn);

        return flag;
    }

    //this method is to be used when attacking from sprinting. sprinting is considered
    //a physical move that makes contact
    private void attackEntityFromSprint(Entity entityIn) {
        if (entityIn == null) return;

        double attackStat = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        int sprintBasePower = 10; //is like this so that i can adjust whenever i need to
        double sprintDamage = attackStat * sprintBasePower * 0.005D;

        entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float) sprintDamage);
        this.setLastAttackedEntity(entityIn);
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

    //-----properties management-----
    public <I> I getProperty(String key) {
        if (!this.propertyValueMap.containsKey(key)) {
            throw new UnsupportedOperationException("Key " + key + " does not exist in property map for " + this.creatureType.getName() + "!");
        }
        return (I) this.propertyValueMap.get(key);
    }

    public <I> void setProperty(String key, I value) {
        AbstractPropertyValue<I> propertyValue = this.getProperty(key);
        if (propertyValue.getHeldClass() != value.getClass()) {
            throw new UnsupportedOperationException("Key " + key + " does not represent given value " + value + "!");
        }
        propertyValue.setValue(value);
        //todo: make this able to sync to client as well
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
        return this.getCreatureMoves().getCurrentMove();
    }

    public void setCurrentMove(String name) {
        CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
        creatureMoveStorage.setCurrentMove(name);
        this.setCreatureMoves(creatureMoveStorage);
    }

    public void resetCurrentMove() {
        CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
        creatureMoveStorage.resetCurrentMove();
        this.setCreatureMoves(creatureMoveStorage);
    }

    //-----IRiftCreature boilerplate stuff-----
    @Override
    @NotNull
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
    @NotNull
    public RiftLibInventoryHandler getCreatureInventory() {
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

    //-----dynamic ride pos related methods-----
    public RiftCreature getDynamicRideUser() {
        return this;
    }

    public float dynamicRiderUserScale() {
        return this.scale();
    }

    //-----ray related methods-----
    public RiftCreature getRayCreator() {
        return this;
    }

    public Map<String, RiftLibRayBuilder> getRayBuilders() {
        return this.rayMap;
    }

    public void applyRaySegments(String rayName, BlockPos rayOrigin, RiftLibRay.RayHitResult rayHitResult) {
        if (this.rayHitEffectMap == null) return;
        this.rayHitEffectMap.get(rayName).accept(this, rayOrigin, rayHitResult);
    }

    public float rayCreatorScale() {
        return this.scale();
    }

    //-----animation related methods-----
    @Override
    public AnimationDataEntity getAnimationData() {
        return this.animData;
    }

    @Override
    public void initializeAnimationData(AnimationDataEntity animationData) {
        //-----create animation controllers-----
        //---for normal stuff---
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntity>(this, "movement", "default",
                new AnimationControllerState<AnimationDataEntity>("default")
                        .addStateTransition("moving", AbstractAnimationDataEntity::isMoving),
                new AnimationControllerState<AnimationDataEntity>("moving", 0.1)
                        .addAnimation("animation."+this.creatureType.getName()+".walk")
                        .addStateTransition("default", animData -> !animData.isMoving())
        ));
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntity>(this, "sprintPosing", "default",
                new AnimationControllerState<AnimationDataEntity>("default", 0.2)
                        .addStateTransition("sprint", animData -> this.isSprinting()),
                new AnimationControllerState<AnimationDataEntity>("sprint", 0.2)
                        .addAnimation("animation."+this.creatureType.getName()+".sprint_pose")
                        .addStateTransition("default", animData -> !this.isSprinting())
        ));
        //---for moves---
        //start with default
        this.initAnimControllerForPhase(animationData, "");
        //now to the other phases
        for (Map.Entry<String, CreaturePhaseBuilder> phase : this.creatureType.getPhaseBuilderMaps().entrySet()) {
            this.initAnimControllerForPhase(animationData, phase.getKey());
        }

        //-----create animation message effects-----
        animationData.addAnimationMessageEffect("moveHitEffect", new AnimatableRunValue(() -> {
            CreatureMoveBuilder creatureMoveBuilder = this.getCreatureMoves().getMoveBuilderCurrentMove();
            if (creatureMoveBuilder == null) return;
            creatureMoveBuilder.getOnMoveHitEffect().accept(this);
        }, Side.SERVER));
        animationData.addAnimationMessageEffect("endMoveEffect", new AnimatableRunValue(() -> {
            //execute on move end
            CreatureMoveBuilder creatureMoveBuilder = this.getCreatureMoves().getMoveBuilderCurrentMove();
            if (creatureMoveBuilder != null && creatureMoveBuilder.getOnMoveEndEffect() != null) creatureMoveBuilder.getOnMoveEndEffect().accept(this);

            //reset current move
            this.resetCurrentMove();
        }, Side.CLIENT, Side.SERVER));
    }

    private void initAnimControllerForPhase(AnimationDataEntity animationData, @NotNull String phase) {
        List<AnimationControllerState<AnimationDataEntity>> creatureMovesStates = new ArrayList<>();
        List<ImmutablePair<String, CreatureMoveBuilder>> moveBuilderMap;
        if (phase.isEmpty()) moveBuilderMap = this.creatureType.getMoves();
        else moveBuilderMap = this.creatureType.getPhaseBuilderMaps().get(phase).getMoves();

        //add the initial state
        AnimationControllerState<AnimationDataEntity> initialState = new AnimationControllerState<>("default");
        creatureMovesStates.add(initialState);

        //add other anim states for each move
        for (ImmutablePair<String, CreatureMoveBuilder> moveEntry : moveBuilderMap) {
            final String moveName = moveEntry.getKey();

            //transition from initial state to a state associated with the move
            initialState.addStateTransition(moveName, animData -> this.getCurrentMove().equals(moveName));

            //create state for move
            AnimationControllerState<AnimationDataEntity> moveState = new AnimationControllerState<AnimationDataEntity>(moveName)
                    .addStateTransition("default", animData -> animData.allAnimationsFinished("moveUse"))
                    .addExitEffect(new AnimatableValue("'endMoveEffect'"));

            //if the move state has multiple animation names, make it so that upon entry it generates a random number
            //to then use
            String[] moveAnimNames = moveEntry.getValue().getAnimNames();
            if (moveAnimNames.length > 1) {
                moveState.addEntryEffect(new AnimatableValue("chosenMove", (double) this.rand.nextInt(moveAnimNames.length)));
            }

            //iterate over each of the anim names and put them in the state for the move
            for (int index = 0; index < moveAnimNames.length; index++) {
                String moveAnimName = moveAnimNames[index];
                moveAnimName = "animation." + this.creatureType.getName() + "." + moveAnimName;

                //only 1 move, just add the move anim name
                if (moveAnimNames.length == 1) moveState.addAnimation(moveAnimName);
                    //multiple moves, add the move anim name and a predicate that uses the chosenMove molang variable above
                else {
                    int finalIndex = index;
                    moveState.addAnimation(moveAnimName, animData -> {
                        return animData.getVariable("chosenMove") == finalIndex;
                    });
                }
            }

            //now add the final state
            creatureMovesStates.add(moveState);
        }

        //now create animation controller for phase
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntity>(this, "moveUse"+phase, "default",
                creatureMovesStates.toArray(new AnimationControllerState[0])
        ));
    }

    //-----other useless events idk nor care about-----
    @Override
    public @Nullable EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
