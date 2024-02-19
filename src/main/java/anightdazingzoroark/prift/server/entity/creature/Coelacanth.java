package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Coelacanth extends RiftWaterCreature {
    public Coelacanth(World worldIn) {
        super(worldIn, RiftCreatureType.COELACANTH);
        this.minCreatureHealth = 6D;
        this.maxCreatureHealth = 6D;
        this.setSize(0.5f, 1f);
        this.experienceValue = 3;
        this.spawnableBlock = Blocks.WATER;
        this.speed = 0.5D;
    }

    protected void initEntityAI() {
        this.tasks.addTask(2, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(3, new RiftHerdMemberFollow(this, 3D, 1D, 1D));
        this.tasks.addTask(4, new RiftWanderWater(this, 1.0D));
    }

    @Override
    public void resetParts(float scale) {}

    public boolean getCanSpawnHere() {
        return this.world.getBlockState(this.getPosition()).getMaterial() == Material.WATER;
    }

    @Override
    public boolean canDoHerding() {
        return this.isInWater();
    }

    @Override
    public float getRenderSizeModifier() {
        return 0;
    }

    @Override
    public float getEyeHeight() { return this.height * 0.05f; }

    @Override
    public Vec3d riderPos() {
        return null;
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
    public boolean isAmphibious() {
        return false;
    }

    @Override
    protected boolean canDespawn() {
        return true;
    }

    @Override
    public void registerControllers(AnimationData data) {

    }
}
