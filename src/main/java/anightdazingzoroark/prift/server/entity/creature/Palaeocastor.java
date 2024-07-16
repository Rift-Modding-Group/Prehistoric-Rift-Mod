package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Palaeocastor extends RiftCreature {
    public Palaeocastor(World worldIn) {
        super(worldIn, RiftCreatureType.PALAEOCASTOR);
        this.setSize(1f, 1f);
    }

    @Override
    public void resetParts(float scale) {

    }

    @Override
    public float getRenderSizeModifier() {
        return 0;
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {

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
}
