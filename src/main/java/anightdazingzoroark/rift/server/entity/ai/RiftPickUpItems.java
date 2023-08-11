package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.RiftCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Arrays;
import java.util.List;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;

public class RiftPickUpItems extends EntityAIBase {
    protected final RiftCreature mob;
    protected final Predicate<? super EntityItem> items;
    protected final boolean checkSight;
    protected final boolean onlyNearby;

    public RiftPickUpItems(RiftCreature mob, String[] items, boolean checkSight) {
        this(mob, items, checkSight, false);
    }

    public RiftPickUpItems(RiftCreature mob, String[] items, boolean checkSight, boolean onlyNearby) {
        this.mob = mob;
        this.checkSight = checkSight;
        this.onlyNearby = onlyNearby;
        this.items = new Predicate<EntityItem>() {
            @Override
            public boolean apply(@Nullable EntityItem entityItem) {
                String itemName = Item.REGISTRY.getNameForObject(entityItem.getItem().getItem()).toString();
                return entityItem != null && !entityItem.getItem().isEmpty() && Arrays.asList(items).contains(itemName);
            }
        };
    }

    @Override
    public boolean shouldExecute() {
        if (this.mob.getHeldItemMainhand().isEmpty()) {
            return true;
        }
        else if (this.mob.getAttackTarget() == null && this.mob.getLastAttackedEntity() == null) {
            List<EntityItem> list = this.mob.world.getEntitiesWithinAABB(EntityItem.class, this.getTargetableArea(this.mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue()), this.items);
            return !list.isEmpty();
        }
        else {
            return false;
        }
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
