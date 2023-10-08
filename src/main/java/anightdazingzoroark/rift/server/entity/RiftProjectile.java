package anightdazingzoroark.rift.server.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public abstract class RiftProjectile extends Entity implements IProjectile, IAnimatable {
    public AnimationFactory factory = new AnimationFactory(this);

    public RiftProjectile(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {

    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {

    }

    @Override
    public abstract void registerControllers(AnimationData data);

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
