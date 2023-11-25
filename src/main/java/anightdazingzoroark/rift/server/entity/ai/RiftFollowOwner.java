package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import anightdazingzoroark.rift.server.enums.TameStatusType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RiftFollowOwner extends EntityAIFollowOwner {
    World world;
    private final RiftCreature tameable;

    public RiftFollowOwner(RiftCreature tameableIn, double followSpeedIn, float minDistIn, float maxDistIn) {
        super(tameableIn, followSpeedIn, minDistIn, maxDistIn);
        this.tameable = tameableIn;
        this.world = tameableIn.world;
    }

    public boolean shouldExecute() {
        if (this.tameable.getTameStatus() != TameStatusType.STAND) {
            return false;
        }
        else if (this.tameable.isStartCharging() || this.tameable.isCharging() || this.tameable.isLoweringHead() || this.tameable.isEndCharging()) {
            return false;
        }
        return super.shouldExecute();
    }

    public boolean shouldContinueExecuting() {
        return this.tameable.getTameStatus() != TameStatusType.STAND && !this.tameable.isStartCharging() && !this.tameable.isCharging() && !this.tameable.isLoweringHead() && !this.tameable.isEndCharging() && super.shouldContinueExecuting();
    }
}
