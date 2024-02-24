package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.RiftHerdMemberFollow;
import anightdazingzoroark.prift.server.entity.ai.RiftWanderWater;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Megapiranha extends RiftWaterCreature {
    public Megapiranha(World worldIn) {
        super(worldIn, RiftCreatureType.MEGAPIRANHA);
        this.setSize(0.5f, 1f);
        this.experienceValue = 3;
        this.speed = 0.5D;
    }

    protected void initEntityAI() {
        this.tasks.addTask(3, new RiftHerdMemberFollow(this));
        this.tasks.addTask(4, new RiftWanderWater(this, 1.0D));
    }

    @Override
    public void resetParts(float scale) {

    }

    @Override
    public boolean canDoHerding() {
        return this.isInWater();
    }

    @Override
    public float getRenderSizeModifier() {
        return 1f;
    }

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
    public void registerControllers(AnimationData data) {

    }

    @Override
    public boolean isAmphibious() {
        return false;
    }
}
