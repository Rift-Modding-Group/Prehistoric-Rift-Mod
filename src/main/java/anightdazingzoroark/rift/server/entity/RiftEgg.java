package anightdazingzoroark.rift.server.entity;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public class RiftEgg extends EntityTameable implements IAnimatable {
    public RiftCreatureType creatureType;
    private static final DataParameter<Integer> HATCH_TIME = EntityDataManager.<Integer>createKey(RiftEgg.class, DataSerializers.VARINT);
    public AnimationFactory factory = new AnimationFactory(this);

    public RiftEgg(World worldIn) {
        this(worldIn, RiftCreatureType.TYRANNOSAURUS);
    }

    public RiftEgg(World worldIn, RiftCreatureType creatureType) {
        super(worldIn);
        this.creatureType = creatureType;
        this.setSize(1F, 1F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HATCH_TIME, Integer.valueOf(300 * 20));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        this.setHatchTime(this.getHatchTime() - 1);
        if (this.getHatchTime() <= 0) {
            RiftCreature creature = this.creatureType.invokeClass(this.world);
            if (creature != null) {
                creature.setGrowingAge(0);
                creature.setTamed(true);
                creature.setOwnerId(this.getOwnerId());
                creature.setLocationAndAngles(Math.floor(this.posX), Math.floor(this.posY) + 1, Math.floor(this.posZ), this.world.rand.nextFloat() * 360.0F, 0.0F);
                if (!this.world.isRemote) {
                    this.world.spawnEntity(creature);
                }
                this.setDead();
            }
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("HatchTime", this.getHatchTime());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setHatchTime(compound.getInteger("HatchTime"));
    }

    public int getHatchTime() {
        return this.dataManager.get(HATCH_TIME).intValue();
    }

    public void setHatchTime(int time) {
        this.dataManager.set(HATCH_TIME, time);
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
