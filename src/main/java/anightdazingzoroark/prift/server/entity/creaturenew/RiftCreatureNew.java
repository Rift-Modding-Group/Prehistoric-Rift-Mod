package anightdazingzoroark.prift.server.entity.creaturenew;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.dataSerializers.RiftDataSerializers;
import anightdazingzoroark.prift.server.entity.aiNew.RiftLookAroundNew;
import anightdazingzoroark.prift.server.entity.aiNew.RiftWanderNew;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.AbstractCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.CreaturePhaseBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.info.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.inventory.CreatureInventoryHandler;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public abstract class RiftCreatureNew extends EntityTameable implements IAnimatable<AnimationDataEntity>, /* IMultiHitboxUser, IDynamicRideUser,*/ IRiftCreature {
    private final RiftCreatureBuilder creatureType;
    private final CreatureInventoryHandler creatureInventory;
    private final AnimationDataEntity factory = new AnimationDataEntity(this);

    private static final IAttribute ELEMENTAL_DAMAGE = new RangedAttribute(null, "rift.elementalDamage", 2.0, 0.0, 2048.0);
    private static final IAttribute STAMINA = new RangedAttribute(null, "rift.stamina", 2.0, 0.0, 2048.0);

    private static final DataParameter<Integer> LEVEL = EntityDataManager.createKey(RiftCreatureNew.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.createKey(RiftCreatureNew.class, DataSerializers.VARINT);
    private static final DataParameter<Float> STAMINA_CURRENT = EntityDataManager.createKey(RiftCreatureNew.class, DataSerializers.FLOAT);
    private static final DataParameter<CreatureMoveStorage> CREATURE_MOVES = EntityDataManager.createKey(RiftCreatureNew.class, RiftDataSerializers.CREATURE_MOVE_STORAGE);

    public RiftCreatureNew(World worldIn, String creatureName) {
        super(worldIn);
        this.creatureType = RiftCreatureRegistry.getCreatureBuilder(creatureName);
        this.setSize(this.creatureType.getMainHitboxSize()[0], this.creatureType.getMainHitboxSize()[1]);
        this.creatureInventory = new CreatureInventoryHandler(this.creatureType.getInventorySize());
        this.parseStats();
        if (!this.creatureType.getCanBeKnockedBack()) this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEVEL, 1);
        this.dataManager.register(AGE_TICKS, 0);
        this.dataManager.register(STAMINA_CURRENT, 0f);
        this.dataManager.register(CREATURE_MOVES, new CreatureMoveStorage());
    }

    //this is gonna be mostly for registering the custom attributes
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getAttributeMap().registerAttribute(ELEMENTAL_DAMAGE);
        this.getAttributeMap().registerAttribute(STAMINA);
    }

    //this turns the stats of a creature into real usable values
    private void parseStats() {
        //parse health
        double healthStat = this.creatureType.getStats().get(RiftCreatureEnums.Stats.HEALTH);
        double finalHealth = RiftUtil.slopeResult(healthStat, false, 0, 10, 0, 200);
        finalHealth += finalHealth * 0.1D * (this.getLevel() - 1);
        finalHealth = Math.round(finalHealth);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(finalHealth);
        this.heal((float) finalHealth);

        //parse melee damage
        double meleeAttackStat = this.creatureType.getStats().get(RiftCreatureEnums.Stats.MELEE_DAMAGE);
        double finalMeleeAttack = RiftUtil.slopeResult(meleeAttackStat, false, 0, 10, 0, 25);
        finalMeleeAttack += finalMeleeAttack * 0.1D * (this.getLevel() - 1);
        finalMeleeAttack = Math.round(finalMeleeAttack);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(finalMeleeAttack);

        //parse elemental damage
        double elementalAttackStat = this.creatureType.getStats().get(RiftCreatureEnums.Stats.ELEMENTAL_DAMAGE);
        double finalElementalAttack = RiftUtil.slopeResult(elementalAttackStat, false, 0, 10, 0, 25);
        finalElementalAttack += finalElementalAttack * 0.1D * (this.getLevel() - 1);
        finalElementalAttack = Math.round(finalElementalAttack);
        this.getEntityAttribute(ELEMENTAL_DAMAGE).setBaseValue(finalElementalAttack);

        //parse stamina
        double staminaStat = this.creatureType.getStats().get(RiftCreatureEnums.Stats.STAMINA);
        double finalStamina = RiftUtil.slopeResult(staminaStat, false, 0, 10, 0, 80);
        finalStamina += finalStamina * 0.1 * (this.getLevel() - 1);
        finalStamina = Math.round(finalStamina);
        this.getEntityAttribute(STAMINA).setBaseValue(finalStamina);

        //parse speed
        double speedStat = this.creatureType.getStats().get(RiftCreatureEnums.Stats.SPEED);
        double finalSpeed = RiftUtil.slopeResult(speedStat, false, 1, 5, 0.15D, 0.35D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(finalSpeed);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setAgeInTicks(this.creatureType.getDaysUntilAdult() * 24000);
        //for move storage initialization
        CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
        creatureMoveStorage.initLearnableMoves(this.creatureType.getLearnableMoves());
        creatureMoveStorage.initUsableMovesPerPhase(this.creatureType.getInitUsableMovesPerPhase());
        this.setCreatureMoves(creatureMoveStorage);

        //return value
        return super.onInitialSpawn(difficulty, livingdata);
    }

    //this is temporary for testing purposes, will be replaced w something more dynamic
    //after being developed further
    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new RiftWanderNew(this, 1D));
        this.tasks.addTask(1, new RiftLookAroundNew(this));
    }

    //this gets the scale of the model of the entity
    public float scale() {
        return RiftUtil.slopeResult(
                this.getAgeInTicks(), true,
                0, this.creatureType.getDaysUntilAdult() * 24000,
                this.creatureType.getScaleRangeForAge()[0], this.creatureType.getScaleRangeForAge()[1]
        );
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
        return (float) this.getEntityAttribute(STAMINA).getAttributeValue();
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

    }

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.factory;
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
