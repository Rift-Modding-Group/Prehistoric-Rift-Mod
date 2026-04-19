package anightdazingzoroark.prift.server.entity.creaturenew;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creaturenew.info.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.inventory.CreatureInventoryHandler;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public abstract class RiftCreatureNew extends EntityTameable implements /*IAnimatable, IMultiHitboxUser, IDynamicRideUser,*/ IRiftCreature {
    private final RiftCreatureBuilder creatureType;
    public final CreatureInventoryHandler creatureInventory;
    //private final AnimationFactory factory = new AnimationFactory(this);

    private static final DataParameter<Integer> LEVEL = EntityDataManager.createKey(RiftCreatureNew.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.createKey(RiftCreatureNew.class, DataSerializers.VARINT);

    public RiftCreatureNew(World worldIn, String creatureName) {
        super(worldIn);
        this.creatureType = RiftCreatureRegistry.getCreatureBuilder(creatureName);
        this.setSize(this.creatureType.getMainHitboxSize()[0], this.creatureType.getMainHitboxSize()[1]);
        this.creatureInventory = new CreatureInventoryHandler(this.creatureType.getInventorySize());
        this.parseStats();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEVEL, 1);
        this.dataManager.register(AGE_TICKS, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    }

    private void parseStats() {}

    //this gets the scale of the model of the entity
    public float getScale() {
        return RiftUtil.slopeResult(
                this.getAgeInTicks(), true,
                0, this.creatureType.getDaysUntilAdult() * 24000,
                this.creatureType.getScaleRangeForAge()[0], this.creatureType.getScaleRangeForAge()[1]
        );
    }

    //-----IRiftCreature stuff-----
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
    /*
    @Override
    public void registerControllers(AnimationData animationData) {

    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
     */

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
