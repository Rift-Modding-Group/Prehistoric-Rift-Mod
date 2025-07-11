package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.config.RiftCreatureConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;

public class RiftPickUpFavoriteFoods extends EntityAIBase {
    protected final RiftCreature mob;
    protected final Predicate<? super EntityItem> items;
    protected final boolean checkSight;
    protected final boolean onlyNearby;

    public RiftPickUpFavoriteFoods(RiftCreature mob, boolean checkSight) {
        this(mob, checkSight, false);
    }

    public RiftPickUpFavoriteFoods(RiftCreature mob, boolean checkSight, boolean onlyNearby) {
        this.mob = mob;
        this.checkSight = checkSight;
        this.onlyNearby = onlyNearby;
        this.items = new Predicate<EntityItem>() {
            @Override
            public boolean apply(@Nullable EntityItem entityItem) {
                ItemStack stackFromEntityItem = entityItem.getItem();
                boolean flag = false;
                for (RiftCreatureConfig.Food food : mob.favoriteFood) {
                    if (!flag) flag = RiftUtil.itemStackEqualToString(stackFromEntityItem, food.itemId);
                }
                return flag;
            }
        };
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return !this.mob.isSleeping() && this.mob.getHeldItemMainhand().isEmpty() && !this.mob.isTamed() && !this.mob.isBeingRidden() && this.mob.getAttackTarget() == null;
    }

    @Override
    public void startExecuting() {
        List<EntityItem> list = this.mob.world.getEntitiesWithinAABB(EntityItem.class, this.getTargetableArea(this.mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue()), this.items);
        if (!list.isEmpty()) {
            this.mob.getNavigator().tryMoveToEntityLiving(list.get(0), 1f);
        }
    }

    @Override
    public void updateTask() {
        List<EntityItem> list = this.mob.world.getEntitiesWithinAABB(EntityItem.class, this.getTargetableArea(this.mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue()), this.items);
        ItemStack itemstack = this.mob.getHeldItemMainhand();
        if (!list.isEmpty() && itemstack.isEmpty()) {
            this.mob.getNavigator().tryMoveToEntityLiving(list.get(0), 1f);
        }
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.mob.getEntityBoundingBox().grow(targetDistance, targetDistance, targetDistance);
    }
}
